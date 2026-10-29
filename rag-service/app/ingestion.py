"""Builds the Chroma vector store from the pages listed in sources.json.

Run it explicitly, with the server stopped:

    python -m app.ingestion

The new store is built in a temporary directory and only swapped into place
once every page has loaded, so a failed or interrupted run never leaves the
server with a half-built store.
"""

import argparse
import gc
import logging
import os
import shutil
import stat
import sys
from pathlib import Path
from urllib.parse import urljoin

import html2text
import requests
from bs4 import BeautifulSoup
from chromadb.api.client import SharedSystemClient
from chromadb.config import Settings as ChromaSettings
from chromadb.utils.embedding_functions import ONNXMiniLM_L6_V2
from langchain_chroma import Chroma
from langchain_core.documents import Document
from langchain_core.embeddings import Embeddings
from langchain_text_splitters import RecursiveCharacterTextSplitter

from .config import configure_logging, settings
from .data import load_sources_config

logger = logging.getLogger(__name__)

REQUEST_TIMEOUT_SECONDS = 30
USER_AGENT = "HW-Student-Assistant-Ingest/1.0"
# Chroma sends anonymous usage stats to PostHog by default
CHROMA_SETTINGS = ChromaSettings(anonymized_telemetry=False)


class IngestionError(RuntimeError):
    pass


class MiniLMEmbeddings(Embeddings):
    """all-MiniLM-L6-v2 run by Chroma's bundled ONNX runtime.

    Gives the same vectors as the sentence-transformers model without
    installing PyTorch, which roughly halves the Docker image.
    """

    def __init__(self) -> None:
        self._model = ONNXMiniLM_L6_V2()

    def embed_documents(self, texts: list[str]) -> list[list[float]]:
        return [vector.tolist() for vector in self._model(texts)]

    def embed_query(self, text: str) -> list[float]:
        return self.embed_documents([text])[0]


def _embeddings() -> MiniLMEmbeddings:
    return MiniLMEmbeddings()


def _fetch_html(session: requests.Session, url: str) -> str:
    response = session.get(url, timeout=REQUEST_TIMEOUT_SECONDS)
    response.raise_for_status()
    return response.text


def _html_to_markdown(html: str, url: str, md_converter: html2text.HTML2Text) -> str:
    soup = BeautifulSoup(html, "html.parser")

    for a_tag in soup.find_all("a", href=True):
        a_tag["href"] = urljoin(url, a_tag["href"])

    main_content = soup.find(["main", "article"])
    return md_converter.handle(str(main_content) if main_content else str(soup))


def _load_and_tag_documents(sources_config: dict) -> tuple[list[Document], list[str]]:
    """Returns (documents, urls_that_failed)."""
    all_docs: list[Document] = []
    failed_urls: list[str] = []

    md_converter = html2text.HTML2Text()
    md_converter.ignore_links = False
    md_converter.ignore_images = True

    session = requests.Session()
    session.headers["User-Agent"] = USER_AGENT

    for source in sources_config.get("sources", []):
        if source.get("type") != "web":
            continue

        urls = source.get("urls", [])
        name = source.get("name", "Unknown")
        logger.info("Loading %d URL(s) for source '%s'", len(urls), name)

        for url in urls:
            try:
                content = _html_to_markdown(_fetch_html(session, url), url, md_converter)
            except Exception as exc:
                logger.error("Failed to load %s: %s", url, exc)
                failed_urls.append(url)
                continue

            if not content.strip():
                logger.error("Page %s has no text content", url)
                failed_urls.append(url)
                continue

            metadata = {**source.get("metadata", {}), "source": url, "source_name": name}
            all_docs.append(Document(page_content=content, metadata=metadata))

    return all_docs, failed_urls


def _release_chroma_files() -> None:
    # Chroma keeps its SQLite files open for as long as the client is cached,
    # and Windows refuses to rename a directory with open files in it.
    for system in SharedSystemClient._identifier_to_system.values():
        system.stop()
    SharedSystemClient.clear_system_cache()
    gc.collect()


def _remove_dir(path: Path) -> None:
    # On Windows (and especially inside OneDrive-synced folders) some entries
    # are marked read-only, which rmtree can't delete until the flag is cleared.
    def clear_readonly_and_retry(func, failed_path, _exc):
        os.chmod(failed_path, stat.S_IWRITE)
        func(failed_path)

    shutil.rmtree(path, onexc=clear_readonly_and_retry)


def _remove_old_dir(old_dir: Path) -> None:
    try:
        _remove_dir(old_dir)
    except OSError as exc:
        logger.warning("Could not delete %s (%s); it is safe to delete it manually", old_dir, exc)


def _swap_into_place(new_dir: Path, target_dir: Path) -> None:
    old_dir = target_dir.with_name(target_dir.name + "_old")
    if old_dir.exists():
        _remove_old_dir(old_dir)
    if old_dir.exists():
        raise IngestionError(f"{old_dir} is left over from a previous run and could not be deleted; delete it manually")
    if target_dir.exists():
        target_dir.rename(old_dir)
    new_dir.rename(target_dir)
    if old_dir.exists():
        _remove_old_dir(old_dir)


def ingest(allow_partial: bool = False) -> int:
    """Scrapes every source and replaces the vector store. Returns the chunk count."""
    sources_config = load_sources_config()
    all_docs, failed_urls = _load_and_tag_documents(sources_config)

    if failed_urls and not allow_partial:
        raise IngestionError(
            f"{len(failed_urls)} page(s) failed to load, so the existing vector store was left unchanged:\n  "
            + "\n  ".join(failed_urls)
            + "\nFix or remove them in sources.json, or rerun with --allow-partial."
        )
    if not all_docs:
        raise IngestionError("Ingestion produced zero documents; refusing to build an empty vector store")

    text_splitter = RecursiveCharacterTextSplitter(
        chunk_size=1000,
        chunk_overlap=200,
        separators=["\n\n", "\n", " ", ""],
    )
    splits = text_splitter.split_documents(all_docs)

    target_dir = settings.chroma_persist_dir
    build_dir = target_dir.with_name(target_dir.name + "_tmp")
    if build_dir.exists():
        _remove_dir(build_dir)

    logger.info("Building vector store from %d chunks in %s", len(splits), build_dir)
    store = Chroma.from_documents(
        documents=splits,
        embedding=_embeddings(),
        persist_directory=str(build_dir),
        client_settings=CHROMA_SETTINGS,
    )
    del store
    _release_chroma_files()

    _swap_into_place(build_dir, target_dir)
    logger.info(
        "Vector store ready at %s (%d pages, %d chunks, %d failed page(s))",
        target_dir,
        len(all_docs),
        len(splits),
        len(failed_urls),
    )
    return len(splits)


def load_vectorstore() -> Chroma:
    persist_dir = settings.chroma_persist_dir
    if not persist_dir.exists():
        raise RuntimeError(f"No vector store found at {persist_dir}. Run `python -m app.ingestion` first.")

    logger.info("Loading vector store from %s", persist_dir)
    store = Chroma(
        persist_directory=str(persist_dir),
        embedding_function=_embeddings(),
        client_settings=CHROMA_SETTINGS,
    )
    if not store.get(limit=1)["ids"]:
        raise RuntimeError(f"Vector store at {persist_dir} is empty. Run `python -m app.ingestion` to rebuild it.")
    return store


def main() -> None:
    parser = argparse.ArgumentParser(description="Rebuild the vector store from sources.json.")
    parser.add_argument(
        "--allow-partial",
        action="store_true",
        help="build the store even if some pages fail to load (they are listed in the log)",
    )
    args = parser.parse_args()

    configure_logging()
    try:
        ingest(allow_partial=args.allow_partial)
    except IngestionError as exc:
        logger.error("%s", exc)
        sys.exit(1)


if __name__ == "__main__":
    main()

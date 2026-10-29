import os
import stat
from pathlib import Path

import html2text
import numpy as np
import pytest
import requests
from langchain_core.documents import Document

from app import ingestion
from app.config import settings
from app.ingestion import IngestionError


@pytest.fixture
def md_converter():
    converter = html2text.HTML2Text()
    converter.ignore_links = False
    converter.ignore_images = True
    return converter


@pytest.fixture
def chroma_dir(tmp_path, monkeypatch):
    target = tmp_path / "chroma_db"
    monkeypatch.setattr(settings, "chroma_persist_dir", target)
    return target


def _pages(monkeypatch, pages: dict):
    """Serve `pages` (url -> html, or an exception to raise) instead of the network."""

    def fake_fetch(_session, url):
        page = pages[url]
        if isinstance(page, Exception):
            raise page
        return page

    monkeypatch.setattr(ingestion, "_fetch_html", fake_fetch)


def _web_source(name, urls, metadata=None):
    return {"type": "web", "name": name, "urls": urls, "metadata": metadata or {}}


# --- HTML conversion ---------------------------------------------------------


def test_relative_links_are_made_absolute(md_converter):
    html = '<main><a href="/students/wifi">Wi-Fi</a></main>'

    markdown = ingestion._html_to_markdown(html, "https://www.hw.ac.uk/students/", md_converter)

    assert "[Wi-Fi](https://www.hw.ac.uk/students/wifi)" in markdown


def test_only_main_content_is_kept_when_present(md_converter):
    html = "<nav>Site menu</nav><main><p>Library hours</p></main><footer>Cookies</footer>"

    markdown = ingestion._html_to_markdown(html, "https://x", md_converter)

    assert "Library hours" in markdown
    assert "Site menu" not in markdown
    assert "Cookies" not in markdown


def test_article_is_used_when_there_is_no_main(md_converter):
    html = "<nav>Site menu</nav><article><p>Visa guide</p></article>"

    markdown = ingestion._html_to_markdown(html, "https://x", md_converter)

    assert "Visa guide" in markdown
    assert "Site menu" not in markdown


def test_whole_page_is_used_without_main_or_article(md_converter):
    markdown = ingestion._html_to_markdown("<div>Welcome Week</div>", "https://x", md_converter)

    assert "Welcome Week" in markdown


def test_images_are_dropped(md_converter):
    markdown = ingestion._html_to_markdown('<main><img src="/logo.png" alt="logo"></main>', "https://x", md_converter)

    assert "logo" not in markdown


# --- Loading pages -----------------------------------------------------------


def test_documents_are_tagged_with_source_metadata(monkeypatch):
    _pages(monkeypatch, {"https://a": "<main>Eduroam setup</main>"})
    config = {"sources": [_web_source("IT", ["https://a"], {"domain": "it_support"})]}

    docs, failed = ingestion._load_and_tag_documents(config)

    assert failed == []
    assert len(docs) == 1
    assert "Eduroam setup" in docs[0].page_content
    assert docs[0].metadata == {"domain": "it_support", "source": "https://a", "source_name": "IT"}


def test_failed_and_empty_pages_are_reported(monkeypatch):
    _pages(
        monkeypatch,
        {
            "https://ok": "<main>Content</main>",
            "https://404": requests.HTTPError("404 Not Found"),
            "https://empty": "<main>   </main>",
        },
    )
    config = {"sources": [_web_source("Mixed", ["https://ok", "https://404", "https://empty"])]}

    docs, failed = ingestion._load_and_tag_documents(config)

    assert [d.metadata["source"] for d in docs] == ["https://ok"]
    assert failed == ["https://404", "https://empty"]


def test_non_web_sources_are_skipped(monkeypatch):
    _pages(monkeypatch, {})
    config = {"sources": [{"type": "pdf", "name": "Handbook", "urls": ["https://pdf"]}]}

    assert ingestion._load_and_tag_documents(config) == ([], [])


def test_fetch_html_raises_on_http_error():
    class FakeResponse:
        text = "Not found"

        def raise_for_status(self):
            raise requests.HTTPError("404")

    class FakeSession:
        def get(self, url, timeout):
            return FakeResponse()

    with pytest.raises(requests.HTTPError):
        ingestion._fetch_html(FakeSession(), "https://x")


# --- Embeddings --------------------------------------------------------------


def test_embeddings_return_plain_float_lists(monkeypatch):
    class FakeModel:
        def __call__(self, texts):
            return [np.array([len(text), 0.5], dtype=np.float32) for text in texts]

    monkeypatch.setattr(ingestion, "ONNXMiniLM_L6_V2", FakeModel)
    embeddings = ingestion.MiniLMEmbeddings()

    assert embeddings.embed_documents(["ab", "abc"]) == [[2.0, 0.5], [3.0, 0.5]]
    assert embeddings.embed_query("abcd") == [4.0, 0.5]


# --- ingest() ----------------------------------------------------------------


@pytest.fixture
def fake_chroma(monkeypatch):
    """Replaces Chroma.from_documents with one that just writes a marker file."""
    built = []

    def from_documents(documents, embedding, persist_directory, client_settings):
        assert client_settings.anonymized_telemetry is False
        built.append(documents)
        Path(persist_directory).mkdir()
        (Path(persist_directory) / "chroma.sqlite3").write_text("new")
        return object()

    monkeypatch.setattr(ingestion.Chroma, "from_documents", staticmethod(from_documents))
    monkeypatch.setattr(ingestion, "_embeddings", lambda: None)
    return built


def _sources(monkeypatch, docs_and_failed):
    monkeypatch.setattr(ingestion, "load_sources_config", lambda: {"sources": []})
    monkeypatch.setattr(ingestion, "_load_and_tag_documents", lambda _config: docs_and_failed)


def test_ingest_refuses_to_build_when_a_page_failed(monkeypatch, chroma_dir, fake_chroma):
    _sources(monkeypatch, ([Document(page_content="x", metadata={})], ["https://bad"]))

    with pytest.raises(IngestionError, match="https://bad"):
        ingestion.ingest()

    assert fake_chroma == []
    assert not chroma_dir.exists()


def test_ingest_refuses_to_build_an_empty_store(monkeypatch, chroma_dir, fake_chroma):
    _sources(monkeypatch, ([], []))

    with pytest.raises(IngestionError, match="zero documents"):
        ingestion.ingest()

    assert fake_chroma == []


def test_ingest_allow_partial_builds_without_failed_pages(monkeypatch, chroma_dir, fake_chroma):
    _sources(monkeypatch, ([Document(page_content="x", metadata={})], ["https://bad"]))

    assert ingestion.ingest(allow_partial=True) == 1
    assert (chroma_dir / "chroma.sqlite3").read_text() == "new"


def test_ingest_splits_long_pages_into_chunks(monkeypatch, chroma_dir, fake_chroma):
    long_page = "\n\n".join(f"Paragraph {i} " + "word " * 100 for i in range(10))
    _sources(monkeypatch, ([Document(page_content=long_page, metadata={"source": "https://a"})], []))

    chunk_count = ingestion.ingest()

    assert chunk_count > 1
    assert len(fake_chroma[0]) == chunk_count
    assert all(len(chunk.page_content) <= 1000 for chunk in fake_chroma[0])
    assert all(chunk.metadata["source"] == "https://a" for chunk in fake_chroma[0])


def test_ingest_replaces_existing_store_and_cleans_up(monkeypatch, chroma_dir, fake_chroma):
    chroma_dir.mkdir()
    (chroma_dir / "chroma.sqlite3").write_text("old")
    leftover_tmp = chroma_dir.with_name("chroma_db_tmp")
    leftover_tmp.mkdir()
    _sources(monkeypatch, ([Document(page_content="x", metadata={})], []))

    ingestion.ingest()

    assert (chroma_dir / "chroma.sqlite3").read_text() == "new"
    assert not leftover_tmp.exists()
    assert not chroma_dir.with_name("chroma_db_old").exists()


# --- Swapping the store into place --------------------------------------------


def test_swap_into_empty_target(tmp_path):
    new, target = tmp_path / "new", tmp_path / "target"
    new.mkdir()
    (new / "f").write_text("new")

    ingestion._swap_into_place(new, target)

    assert (target / "f").read_text() == "new"
    assert not new.exists()


def test_swap_removes_stale_old_dir_first(tmp_path):
    new, target = tmp_path / "new", tmp_path / "target"
    new.mkdir()
    target.mkdir()
    (tmp_path / "target_old").mkdir()

    ingestion._swap_into_place(new, target)

    assert target.exists()
    assert not (tmp_path / "target_old").exists()


def test_swap_aborts_if_stale_old_dir_cannot_be_removed(tmp_path, monkeypatch):
    new, target = tmp_path / "new", tmp_path / "target"
    new.mkdir()
    target.mkdir()
    (target / "f").write_text("current")
    (tmp_path / "target_old").mkdir()
    monkeypatch.setattr(ingestion, "_remove_old_dir", lambda _path: None)

    with pytest.raises(IngestionError, match="target_old"):
        ingestion._swap_into_place(new, target)

    assert (target / "f").read_text() == "current"
    assert new.exists()


def test_remove_dir_handles_read_only_files(tmp_path):
    directory = tmp_path / "d"
    directory.mkdir()
    locked = directory / "locked"
    locked.write_text("x")
    os.chmod(locked, stat.S_IREAD)

    ingestion._remove_dir(directory)

    assert not directory.exists()


# --- load_vectorstore() ------------------------------------------------------


def test_load_vectorstore_fails_when_store_is_missing(chroma_dir):
    with pytest.raises(RuntimeError, match="No vector store found"):
        ingestion.load_vectorstore()


def test_load_vectorstore_fails_when_store_is_empty(chroma_dir, monkeypatch):
    chroma_dir.mkdir()

    class EmptyChroma:
        def __init__(self, **_):
            pass

        def get(self, limit):
            return {"ids": []}

    monkeypatch.setattr(ingestion, "Chroma", EmptyChroma)
    monkeypatch.setattr(ingestion, "_embeddings", lambda: None)

    with pytest.raises(RuntimeError, match="is empty"):
        ingestion.load_vectorstore()


def test_load_vectorstore_returns_populated_store(chroma_dir, monkeypatch):
    chroma_dir.mkdir()

    class PopulatedChroma:
        def __init__(self, persist_directory, embedding_function, client_settings):
            self.persist_directory = persist_directory
            self.client_settings = client_settings

        def get(self, limit):
            return {"ids": ["1"]}

    monkeypatch.setattr(ingestion, "Chroma", PopulatedChroma)
    monkeypatch.setattr(ingestion, "_embeddings", lambda: None)

    store = ingestion.load_vectorstore()

    assert store.persist_directory == str(chroma_dir)
    assert store.client_settings.anonymized_telemetry is False

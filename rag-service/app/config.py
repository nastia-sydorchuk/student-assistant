import logging
from pathlib import Path

from pydantic_settings import BaseSettings, SettingsConfigDict

BASE_DIR = Path(__file__).resolve().parent.parent


class Settings(BaseSettings):
    google_api_key: str
    api_auth_key: str | None = None

    llm_model: str = "gemini-3.5-flash-lite"

    chroma_persist_dir: Path = BASE_DIR / "chroma_db"
    sources_path: Path = BASE_DIR / "sources.json"
    contacts_path: Path = BASE_DIR / "contacts.json"

    max_history_messages: int = 4
    session_ttl_seconds: int = 2 * 60 * 60

    model_config = SettingsConfigDict(env_file=str(BASE_DIR / ".env"), extra="ignore")


settings = Settings()


def configure_logging() -> None:
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s %(levelname)s %(name)s: %(message)s",
    )

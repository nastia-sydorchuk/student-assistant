import json
from pathlib import Path
from typing import Any

from .config import settings


def _load_json(path: Path) -> Any:
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)


def load_contacts() -> dict:
    return _load_json(settings.contacts_path)


def load_sources_config() -> dict:
    return _load_json(settings.sources_path)

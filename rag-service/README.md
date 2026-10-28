# HW Student Assistant RAG API

A FastAPI service that answers Heriot-Watt University student questions using
retrieval-augmented generation (Gemini + Chroma), grounded in the pages
listed in `sources.json`.

## Setup

```
python -m venv .venv
.venv\Scripts\activate        # or `source .venv/bin/activate` on macOS/Linux
pip install -r requirements.txt
copy .env.example .env        # or `cp` on macOS/Linux
```

Fill in `GOOGLE_API_KEY` in `.env`. `API_AUTH_KEY` is optional (see
`.env.example`).

## Building the knowledge base

A prebuilt vector store is committed in `chroma_db/`, so the server runs
straight after setup. Rebuild it whenever you edit `sources.json`, or to pick
up changes to the university pages (stop the server first):

```
python -m app.ingestion
```

This scrapes every URL in `sources.json`, embeds the pages, and saves them to
`chroma_db/`. It can take a few minutes. If any page fails to load (network
error, 404, empty page), the command lists the failed URLs, exits with an
error, and leaves the existing `chroma_db/` untouched. To build anyway
without the failed pages, add `--allow-partial`.

The new store is built in `chroma_db_tmp/` and only replaces `chroma_db/` once
it's complete, so an interrupted run can't leave a half-built store behind.

## Running

```
uvicorn app.main:app --reload --port 8000
```

The server only loads `chroma_db/`; it never scrapes. If the store is missing
or empty, it refuses to start and tells you to run the ingestion command.

## Tests

```
pip install -r requirements-dev.txt
pytest
```

The tests run offline: Gemini, the retriever, Chroma, and page fetching are
replaced with fakes, so no API key, vector store, or network is needed.

## Endpoints

- `GET /health` — liveness check.
- `POST /rag-query` — ask a question.

  ```json
  {
    "query": "How do I connect to Eduroam?",
    "session_id": "required UUID (e.g. crypto.randomUUID()), reused for every message in a conversation",
    "bot_greeting": "optional greeting to seed a new session's history"
  }
  ```

  Returns `{"answer": ..., "sources": [...]}`. Send the same `session_id` on
  every message of a conversation to keep conversational context; use a new
  one to start a fresh conversation. Requests with a missing or non-UUID
  `session_id` are rejected with 422.

  Length limits (longer values are rejected with 422): `query` 1–1000
  characters after trimming whitespace, `bot_greeting` 500.

  If `API_AUTH_KEY` is set, requests must include an `X-API-Key` header with
  that value. The backend sends it when its `RAG_API_KEY` env var is set to
  the same value.

## Project layout

- `app/config.py` — settings (env vars, model names, paths).
- `app/data.py` — loads `contacts.json` / `sources.json`.
- `app/ingestion.py` — the `python -m app.ingestion` command that scrapes
  `sources.json` and builds the Chroma vector store, plus the loader the
  server uses at startup.
- `app/chains.py` — prompts, retriever, and the conversational RAG chain.
- `app/sessions.py` — in-memory chat history with TTL-based eviction.
- `app/main.py` — the FastAPI app and routes.
- `tests/` — pytest suite, one file per module.

## Known limitations

- Chat history is in-process memory (`app/sessions.py`), not shared across
  multiple server instances and lost on restart. Fine for a single-instance
  deployment; move to Redis or similar before scaling horizontally.
- No request rate limiting — put this behind a reverse proxy / API gateway
  that enforces it before exposing the service publicly.

import importlib
import sys
from uuid import uuid4

import pytest
from fastapi.testclient import TestClient
from langchain_core.documents import Document
from langchain_core.messages import AIMessage

from app import chains, ingestion
from app.chains import RagAnswer
from app.config import settings
from app.sessions import TTLSessionStore


class FakeChain:
    def __init__(self):
        self.calls = []
        self.response = {"result": RagAnswer(answer="Hello", source_ids=[]), "context": []}
        self.error = None

    async def ainvoke(self, inputs, config):
        self.calls.append((inputs, config))
        if self.error:
            raise self.error
        return self.response


@pytest.fixture(scope="module")
def main_module():
    # app.main loads the vector store and builds the chain at import time, so
    # stub both out before importing it.
    with pytest.MonkeyPatch.context() as mp:
        mp.setattr(ingestion, "load_vectorstore", lambda: None)
        mp.setattr(chains, "build_conversational_chain", lambda *_: None)
        sys.modules.pop("app.main", None)
        yield importlib.import_module("app.main")
    sys.modules.pop("app.main", None)


@pytest.fixture
def fake_chain(main_module, monkeypatch):
    chain = FakeChain()
    monkeypatch.setattr(main_module, "conversational_rag_chain", chain)
    monkeypatch.setattr(main_module, "session_store", TTLSessionStore(ttl_seconds=60, max_messages=10))
    monkeypatch.setattr(settings, "api_auth_key", None)
    return chain


@pytest.fixture
def client(main_module, fake_chain):
    return TestClient(main_module.app)


def _body(**overrides):
    return {"query": "How do I connect to Eduroam?", "session_id": str(uuid4()), **overrides}


def test_health(client):
    response = client.get("/health")

    assert response.status_code == 200
    assert response.json() == {"status": "ok"}


def test_query_returns_answer(client, fake_chain):
    response = client.post("/rag-query", json=_body())

    assert response.status_code == 200
    assert response.json() == {"answer": "Hello", "sources": []}


def test_query_passes_input_and_session_to_chain(client, fake_chain):
    body = _body(query="  Where is the library?  ", current_url="https://www.hw.ac.uk/students/")

    client.post("/rag-query", json=body)

    inputs, config = fake_chain.calls[0]
    assert inputs == {"input": "Where is the library?", "current_url": "https://www.hw.ac.uk/students/"}
    assert config == {"configurable": {"session_id": body["session_id"]}}


def test_current_url_defaults_to_unknown(client, fake_chain):
    client.post("/rag-query", json=_body())

    assert fake_chain.calls[0][0]["current_url"] == "Unknown"


def test_sources_map_ids_to_doc_urls_in_model_order_without_duplicates(client, fake_chain):
    fake_chain.response = {
        "result": RagAnswer(answer="See these", source_ids=[3, 1, 3]),
        "context": [
            Document(page_content="a", metadata={"source": "https://a"}),
            Document(page_content="b", metadata={"source": "https://b"}),
            Document(page_content="c", metadata={"source": "https://c"}),
        ],
    }

    response = client.post("/rag-query", json=_body())

    assert response.json()["sources"] == ["https://c", "https://a"]


def test_sources_ignore_invented_ids_and_docs_without_source(client, fake_chain):
    fake_chain.response = {
        "result": RagAnswer(answer="See these", source_ids=[0, 1, 2, 5, -1]),
        "context": [
            Document(page_content="a", metadata={"source": "https://a"}),
            Document(page_content="b", metadata={}),
        ],
    }

    response = client.post("/rag-query", json=_body())

    assert response.json()["sources"] == ["https://a"]


def test_same_url_from_different_chunks_is_listed_once(client, fake_chain):
    fake_chain.response = {
        "result": RagAnswer(answer="See these", source_ids=[1, 2]),
        "context": [
            Document(page_content="a1", metadata={"source": "https://a"}),
            Document(page_content="a2", metadata={"source": "https://a"}),
        ],
    }

    response = client.post("/rag-query", json=_body())

    assert response.json()["sources"] == ["https://a"]


def test_greeting_seeds_new_session_only(client, main_module):
    body = _body(bot_greeting="Hi! How can I help?")

    client.post("/rag-query", json=body)
    client.post("/rag-query", json=body)

    assert main_module.session_store.get(body["session_id"]).messages == [AIMessage(content="Hi! How can I help?")]


def test_no_greeting_leaves_history_empty(client, main_module):
    body = _body()

    client.post("/rag-query", json=body)

    assert main_module.session_store.get(body["session_id"]).messages == []


def test_chain_failure_returns_502(client, fake_chain):
    fake_chain.error = RuntimeError("Gemini is down")

    response = client.post("/rag-query", json=_body())

    assert response.status_code == 502
    assert response.json() == {"detail": "Failed to generate an answer. Please try again."}


@pytest.mark.parametrize(
    "overrides",
    [
        {"session_id": "not-a-uuid"},
        {"session_id": None},
        {"query": ""},
        {"query": "   "},
        {"query": "x" * 1001},
        {"bot_greeting": "x" * 501},
        {"current_url": "x" * 2049},
    ],
)
def test_invalid_request_is_rejected(client, fake_chain, overrides):
    response = client.post("/rag-query", json=_body(**overrides))

    assert response.status_code == 422
    assert fake_chain.calls == []


def test_missing_session_id_is_rejected(client):
    response = client.post("/rag-query", json={"query": "hi"})

    assert response.status_code == 422


def test_max_length_values_are_accepted(client):
    body = _body(query="x" * 1000, bot_greeting="x" * 500, current_url="x" * 2048)

    assert client.post("/rag-query", json=body).status_code == 200


@pytest.mark.parametrize("headers", [{}, {"X-API-Key": "wrong"}])
def test_auth_rejects_missing_or_wrong_key(client, fake_chain, monkeypatch, headers):
    monkeypatch.setattr(settings, "api_auth_key", "secret")

    response = client.post("/rag-query", json=_body(), headers=headers)

    assert response.status_code == 401
    assert fake_chain.calls == []


def test_auth_accepts_correct_key(client, monkeypatch):
    monkeypatch.setattr(settings, "api_auth_key", "secret")

    response = client.post("/rag-query", json=_body(), headers={"X-API-Key": "secret"})

    assert response.status_code == 200


def test_health_does_not_require_auth(client, monkeypatch):
    monkeypatch.setattr(settings, "api_auth_key", "secret")

    assert client.get("/health").status_code == 200

from types import SimpleNamespace

import pytest
from langchain_core.messages import AIMessage, HumanMessage

from app import sessions
from app.sessions import TTLSessionStore


@pytest.fixture
def clock(monkeypatch):
    now = SimpleNamespace(value=1000.0)
    monkeypatch.setattr(sessions, "time", SimpleNamespace(time=lambda: now.value))
    return now


def test_same_session_id_returns_same_history(clock):
    store = TTLSessionStore(ttl_seconds=60, max_messages=10)

    store.get("a").add_message(HumanMessage(content="hi"))

    assert store.get("a").messages == [HumanMessage(content="hi")]


def test_sessions_are_isolated(clock):
    store = TTLSessionStore(ttl_seconds=60, max_messages=10)

    store.get("a").add_message(HumanMessage(content="hi"))

    assert store.get("b").messages == []


def test_history_is_trimmed_to_last_max_messages(clock):
    store = TTLSessionStore(ttl_seconds=60, max_messages=2)
    history = store.get("a")
    history.add_messages(
        [
            HumanMessage(content="1"),
            AIMessage(content="2"),
            HumanMessage(content="3"),
        ]
    )

    assert [m.content for m in store.get("a").messages] == ["2", "3"]


def test_session_expires_after_ttl(clock):
    store = TTLSessionStore(ttl_seconds=60, max_messages=10)
    store.get("a").add_message(HumanMessage(content="hi"))

    clock.value += 61

    assert store.get("a").messages == []


def test_access_refreshes_ttl(clock):
    store = TTLSessionStore(ttl_seconds=60, max_messages=10)
    store.get("a").add_message(HumanMessage(content="hi"))

    clock.value += 50
    store.get("a")
    clock.value += 50

    assert store.get("a").messages == [HumanMessage(content="hi")]


def test_expired_sessions_are_evicted_when_another_session_is_accessed(clock):
    store = TTLSessionStore(ttl_seconds=60, max_messages=10)
    store.get("a")

    clock.value += 61
    store.get("b")

    assert "a" not in store._histories
    assert "a" not in store._last_access

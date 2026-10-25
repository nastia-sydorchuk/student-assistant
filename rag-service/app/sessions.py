import threading
import time

from langchain_core.chat_history import InMemoryChatMessageHistory


class TTLSessionStore:
    """In-memory chat history store that evicts sessions untouched for `ttl_seconds`
    and keeps only the last `max_messages` messages of each session.

    Without eviction, one entry accumulates per session_id for the life of the
    process, which is an unbounded memory leak for a long-running server.
    """

    def __init__(self, ttl_seconds: int, max_messages: int):
        self._ttl_seconds = ttl_seconds
        self._max_messages = max_messages
        self._histories: dict[str, InMemoryChatMessageHistory] = {}
        self._last_access: dict[str, float] = {}
        self._lock = threading.Lock()

    def get(self, session_id: str) -> InMemoryChatMessageHistory:
        with self._lock:
            self._evict_expired()
            self._last_access[session_id] = time.time()
            history = self._histories.setdefault(session_id, InMemoryChatMessageHistory())
            if len(history.messages) > self._max_messages:
                history.messages = history.messages[-self._max_messages :]
            return history

    def _evict_expired(self) -> None:
        cutoff = time.time() - self._ttl_seconds
        expired = [sid for sid, ts in self._last_access.items() if ts < cutoff]
        for sid in expired:
            self._histories.pop(sid, None)
            self._last_access.pop(sid, None)

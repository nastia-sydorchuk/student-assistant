import logging
from typing import Annotated
from uuid import UUID

from fastapi import Depends, FastAPI, Header, HTTPException
from langchain_core.messages import AIMessage
from pydantic import BaseModel, StringConstraints

from .chains import build_conversational_chain
from .config import configure_logging, settings
from .data import load_contacts
from .ingestion import load_vectorstore
from .sessions import TTLSessionStore

configure_logging()
logger = logging.getLogger(__name__)

app = FastAPI(title="HW Student Assistant RAG API")

contacts = load_contacts()
vectorstore = load_vectorstore()
session_store = TTLSessionStore(ttl_seconds=settings.session_ttl_seconds)
conversational_rag_chain = build_conversational_chain(vectorstore, contacts, session_store.get)


def verify_api_key(x_api_key: str | None = Header(default=None)) -> None:
    if settings.api_auth_key and x_api_key != settings.api_auth_key:
        raise HTTPException(status_code=401, detail="Invalid or missing API key")


class QueryRequest(BaseModel):
    # Every request costs several LLM calls, so cap what a client can send.
    query: Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=1000)]
    current_url: Annotated[str, StringConstraints(max_length=2048)] = "Unknown"
    session_id: UUID
    bot_greeting: Annotated[str, StringConstraints(max_length=500)] | None = None


class QueryResponse(BaseModel):
    answer: str
    sources: list[str]


@app.get("/health")
async def health() -> dict:
    return {"status": "ok"}


@app.post("/rag-query", response_model=QueryResponse, dependencies=[Depends(verify_api_key)])
async def query_rag(request: QueryRequest) -> QueryResponse:
    session_id = str(request.session_id)
    history = session_store.get(session_id)

    if len(history.messages) > settings.max_history_messages:
        history.messages = history.messages[-settings.max_history_messages :]

    if len(history.messages) == 0 and request.bot_greeting:
        history.add_message(AIMessage(content=request.bot_greeting))

    try:
        response = await conversational_rag_chain.ainvoke(
            {"input": request.query, "current_url": request.current_url},
            config={"configurable": {"session_id": session_id}},
        )
    except Exception:
        logger.exception("RAG chain invocation failed for session %s", session_id)
        raise HTTPException(status_code=502, detail="Failed to generate an answer. Please try again.")

    answer = response["answer"]
    used_sources = "[SOURCES_USED]" in answer
    answer = answer.replace("[SOURCES_USED]", "").replace("[NO_SOURCES]", "").strip()

    sources: list[str] = []
    if used_sources:
        sources = list({doc.metadata["source"] for doc in response["context"] if "source" in doc.metadata})

    return QueryResponse(answer=answer, sources=sources)

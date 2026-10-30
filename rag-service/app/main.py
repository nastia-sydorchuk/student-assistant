import logging
from contextlib import asynccontextmanager
from typing import Annotated
from uuid import UUID

from fastapi import Depends, FastAPI, Header, HTTPException, Request
from langchain_core.messages import AIMessage
from langchain_core.runnables import Runnable
from pydantic import BaseModel, StringConstraints

from .chains import RagAnswer, build_conversational_chain
from .config import configure_logging, settings
from .data import load_contacts
from .ingestion import load_vectorstore
from .sessions import TTLSessionStore

configure_logging()
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Runs once before the server accepts requests; a missing or empty vector store stops startup here
    session_store = TTLSessionStore(
        ttl_seconds=settings.session_ttl_seconds,
        max_messages=settings.max_history_messages,
    )
    app.state.session_store = session_store
    app.state.chain = build_conversational_chain(load_vectorstore(), load_contacts(), session_store.get)
    yield


app = FastAPI(title="HW Student Assistant RAG API", lifespan=lifespan)


def get_chain(request: Request) -> Runnable:
    return request.app.state.chain


def get_session_store(request: Request) -> TTLSessionStore:
    return request.app.state.session_store


def verify_api_key(x_api_key: str | None = Header(default=None)) -> None:
    if settings.api_auth_key and x_api_key != settings.api_auth_key:
        raise HTTPException(status_code=401, detail="Invalid or missing API key")


class QueryRequest(BaseModel):
    # Every request costs several LLM calls, so cap what a client can send.
    query: Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=1000)]
    session_id: UUID
    bot_greeting: Annotated[str, StringConstraints(max_length=500)] | None = None


class QueryResponse(BaseModel):
    answer: str
    sources: list[str]


@app.get("/health")
async def health() -> dict:
    return {"status": "ok"}


@app.post("/rag-query", response_model=QueryResponse, dependencies=[Depends(verify_api_key)])
async def query_rag(
    request: QueryRequest,
    chain: Annotated[Runnable, Depends(get_chain)],
    session_store: Annotated[TTLSessionStore, Depends(get_session_store)],
) -> QueryResponse:
    session_id = str(request.session_id)
    history = session_store.get(session_id)

    if len(history.messages) == 0 and request.bot_greeting:
        history.add_message(AIMessage(content=request.bot_greeting))

    try:
        response = await chain.ainvoke(
            {"input": request.query},
            config={"configurable": {"session_id": session_id}},
        )
    except Exception:
        logger.exception("RAG chain invocation failed for session %s", session_id)
        raise HTTPException(status_code=502, detail="Failed to generate an answer. Please try again.")

    result: RagAnswer = response["result"]
    docs = response["context"]

    # source_ids are 1-based positions in the retrieved docs; ignore any the
    # model invented, and dedupe while keeping the model's order.
    sources = list(
        dict.fromkeys(
            docs[i - 1].metadata["source"]
            for i in result.source_ids
            if 1 <= i <= len(docs) and "source" in docs[i - 1].metadata
        )
    )

    return QueryResponse(answer=result.answer, sources=sources)

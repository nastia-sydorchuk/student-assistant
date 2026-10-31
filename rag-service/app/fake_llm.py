from langchain_core.documents import Document
from langchain_core.runnables import Runnable, RunnableLambda

from .chains import RagAnswer

# e2e/tests/chat.spec.ts asserts on these two values
CANNED_ANSWER = "This is a canned answer from the fake LLM."
CANNED_SOURCE = "https://www.hw.ac.uk/students/fake-llm-source"


def build_fake_chain() -> Runnable:
    """Stands in for the whole RAG chain in end-to-end tests: no vector store, no Gemini calls."""
    return RunnableLambda(
        lambda _: {
            "result": RagAnswer(answer=CANNED_ANSWER, source_ids=[1]),
            "context": [Document(page_content=CANNED_ANSWER, metadata={"source": CANNED_SOURCE})],
        }
    )

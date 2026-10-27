import pytest
from langchain_core.documents import Document
from langchain_core.language_models.fake_chat_models import FakeListChatModel
from langchain_core.messages import AIMessage, HumanMessage, SystemMessage
from langchain_core.retrievers import BaseRetriever
from langchain_core.runnables import RunnableLambda

from app import chains
from app.chains import RagAnswer, _format_docs, build_conversational_chain
from app.sessions import TTLSessionStore

CONTACTS = {"it_support": "it@example.ac.uk", "health_wellbeing": "wellbeing@example.ac.uk"}
DOCS = [
    Document(page_content="Connect to eduroam with your HW username.", metadata={"source": "https://a"}),
    Document(page_content="The library is open 24/7.", metadata={"source": "https://b"}),
]

# Messages of every prompt sent to the structured-output (answer) call.
qa_prompts: list = []


class FakeRetriever(BaseRetriever):
    docs: list[Document]
    queries: list[str] = []

    def _get_relevant_documents(self, query, *, run_manager):
        self.queries.append(query)
        return self.docs


class FakeLLM(FakeListChatModel):
    """Plain calls (the query rewriter) return the scripted responses; the
    structured-output call records the prompt it was given and returns a
    fixed RagAnswer."""

    def with_structured_output(self, schema, **kwargs):
        def respond(prompt_value):
            qa_prompts.append(prompt_value.to_messages())
            return schema(answer="Use eduroam.", source_ids=[1])

        return RunnableLambda(respond)


@pytest.fixture
def chain_parts(monkeypatch):
    qa_prompts.clear()
    retriever = FakeRetriever(docs=DOCS)
    monkeypatch.setattr(chains, "ChatGoogleGenerativeAI", lambda **_: FakeLLM(responses=["rewritten query"]))
    monkeypatch.setattr(chains.SelfQueryRetriever, "from_llm", lambda **_: retriever)
    store = TTLSessionStore(ttl_seconds=60, max_messages=10)
    chain = build_conversational_chain(vectorstore=None, contacts=CONTACTS, get_session_history=store.get)
    return chain, retriever, store


def _ask(chain, question, session_id="s1"):
    return chain.invoke(
        {"input": question},
        config={"configurable": {"session_id": session_id}},
    )


def test_format_docs_numbers_sources_from_one():
    assert _format_docs(DOCS) == (
        "[Source 1]\nConnect to eduroam with your HW username.\n\n[Source 2]\nThe library is open 24/7."
    )


def test_format_docs_empty():
    assert _format_docs([]) == ""


def test_chain_returns_structured_result_and_raw_docs(chain_parts):
    chain, _, _ = chain_parts

    response = _ask(chain, "How do I get on Wi-Fi?")

    assert response["result"] == RagAnswer(answer="Use eduroam.", source_ids=[1])
    assert response["answer"] == "Use eduroam."
    assert response["context"] == DOCS


def test_prompt_contains_numbered_context_and_contacts(chain_parts):
    chain, _, _ = chain_parts

    _ask(chain, "How do I get on Wi-Fi?")

    system = qa_prompts[0][0]
    assert isinstance(system, SystemMessage)
    assert "[Source 1]\nConnect to eduroam" in system.content
    assert "[Source 2]\nThe library is open 24/7." in system.content
    assert "it@example.ac.uk" in system.content
    assert "contact the Student Wellbeing team at wellbeing@example.ac.uk" in system.content


def test_first_question_is_used_as_search_query_directly(chain_parts):
    chain, retriever, _ = chain_parts

    _ask(chain, "How do I get on Wi-Fi?")

    assert retriever.queries == ["How do I get on Wi-Fi?"]


def test_follow_up_question_is_rewritten_using_history(chain_parts):
    chain, retriever, _ = chain_parts

    _ask(chain, "How do I get on Wi-Fi?")
    _ask(chain, "what about on my phone?")

    assert retriever.queries == ["How do I get on Wi-Fi?", "rewritten query"]


def test_question_and_answer_are_saved_to_session_history(chain_parts):
    chain, _, store = chain_parts

    _ask(chain, "How do I get on Wi-Fi?")

    assert store.get("s1").messages == [
        HumanMessage(content="How do I get on Wi-Fi?"),
        AIMessage(content="Use eduroam."),
    ]
    assert store.get("other").messages == []


def test_history_is_passed_to_the_answer_prompt(chain_parts):
    chain, _, _ = chain_parts

    _ask(chain, "How do I get on Wi-Fi?")
    _ask(chain, "what about on my phone?")

    assert [m.content for m in qa_prompts[1][1:]] == [
        "How do I get on Wi-Fi?",
        "Use eduroam.",
        "what about on my phone?",
    ]

import pytest
from langchain_core.documents import Document
from langchain_core.language_models.fake_chat_models import FakeListChatModel
from langchain_core.messages import AIMessage, HumanMessage, SystemMessage
from langchain_core.runnables import RunnableLambda
from pydantic import ValidationError

from app import chains
from app.chains import RagAnswer, _format_docs, _search_model, build_conversational_chain
from app.sessions import TTLSessionStore

CONTACTS = {"it_support": "it@example.ac.uk", "health_wellbeing": "wellbeing@example.ac.uk"}
DOCS = [
    Document(page_content="Connect to eduroam with your HW username.", metadata={"source": "https://a"}),
    Document(page_content="The library is open 24/7.", metadata={"source": "https://b"}),
]


class FakeVectorStore:
    def __init__(self):
        self.searches = []

    def max_marginal_relevance_search(self, query, filter=None, **kwargs):
        self.searches.append((query, filter))
        return DOCS


class FakeLLM(FakeListChatModel):
    """Records the messages of every structured-output call. The search call returns `search`;
    the answer call returns a fixed RagAnswer."""

    search: dict = {"query": "eduroam Edinburgh campus", "domain": "it_support"}
    search_prompts: list = []
    qa_prompts: list = []

    def with_structured_output(self, schema, **kwargs):
        def respond(prompt_value):
            if schema is RagAnswer:
                self.qa_prompts.append(prompt_value.to_messages())
                return RagAnswer(answer="Use eduroam.", source_ids=[1])
            self.search_prompts.append(prompt_value.to_messages())
            return schema(**self.search)

        return RunnableLambda(respond)


@pytest.fixture
def llm(monkeypatch):
    llm = FakeLLM(responses=[], search_prompts=[], qa_prompts=[])
    monkeypatch.setattr(chains, "ChatGoogleGenerativeAI", lambda **_: llm)
    return llm


@pytest.fixture
def chain_parts(llm):
    vectorstore = FakeVectorStore()
    store = TTLSessionStore(ttl_seconds=60, max_messages=10)
    chain = build_conversational_chain(vectorstore=vectorstore, contacts=CONTACTS, get_session_history=store.get)
    return chain, vectorstore, store


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


def test_search_domain_allows_exactly_the_contact_domains():
    schema = _search_model(list(CONTACTS)).model_json_schema()

    assert schema["properties"]["domain"]["anyOf"][0]["enum"] == ["it_support", "health_wellbeing"]


def test_search_rejects_an_unknown_domain():
    Search = _search_model(list(CONTACTS))

    assert Search(query="q").domain is None
    with pytest.raises(ValidationError):
        Search(query="q", domain="visas")


def test_search_uses_the_generated_query_and_domain_filter(chain_parts):
    chain, vectorstore, _ = chain_parts

    _ask(chain, "How do I get on Wi-Fi?")

    assert vectorstore.searches == [("eduroam Edinburgh campus", {"domain": "it_support"})]


def test_search_without_a_domain_is_unfiltered(chain_parts, llm):
    chain, vectorstore, _ = chain_parts
    llm.search = {"query": "student discounts Edinburgh campus", "domain": None}

    _ask(chain, "Are there student discounts?")

    assert vectorstore.searches == [("student discounts Edinburgh campus", None)]


def test_search_prompt_sees_the_history_for_follow_ups(chain_parts, llm):
    chain, _, _ = chain_parts

    _ask(chain, "How do I get on Wi-Fi?")
    _ask(chain, "what about on my phone?")

    assert [m.content for m in llm.search_prompts[1][1:]] == [
        "How do I get on Wi-Fi?",
        "Use eduroam.",
        "what about on my phone?",
    ]


def test_chain_returns_structured_result_and_raw_docs(chain_parts):
    chain, _, _ = chain_parts

    response = _ask(chain, "How do I get on Wi-Fi?")

    assert response["result"] == RagAnswer(answer="Use eduroam.", source_ids=[1])
    assert response["answer"] == "Use eduroam."
    assert response["context"] == DOCS


def test_prompt_contains_numbered_context_and_contacts(chain_parts, llm):
    chain, _, _ = chain_parts

    _ask(chain, "How do I get on Wi-Fi?")

    system = llm.qa_prompts[0][0]
    assert isinstance(system, SystemMessage)
    assert "[Source 1]\nConnect to eduroam" in system.content
    assert "[Source 2]\nThe library is open 24/7." in system.content
    assert "it@example.ac.uk" in system.content
    assert "contact the Student Wellbeing team at wellbeing@example.ac.uk" in system.content


def test_question_and_answer_are_saved_to_session_history(chain_parts):
    chain, _, store = chain_parts

    _ask(chain, "How do I get on Wi-Fi?")

    assert store.get("s1").messages == [
        HumanMessage(content="How do I get on Wi-Fi?"),
        AIMessage(content="Use eduroam."),
    ]
    assert store.get("other").messages == []


def test_history_is_passed_to_the_answer_prompt(chain_parts, llm):
    chain, _, _ = chain_parts

    _ask(chain, "How do I get on Wi-Fi?")
    _ask(chain, "what about on my phone?")

    assert [m.content for m in llm.qa_prompts[1][1:]] == [
        "How do I get on Wi-Fi?",
        "Use eduroam.",
        "what about on my phone?",
    ]

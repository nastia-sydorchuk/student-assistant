import json
from typing import Callable, Literal

from langchain_chroma import Chroma
from langchain_core.chat_history import BaseChatMessageHistory
from langchain_core.documents import Document
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain_core.runnables import Runnable, RunnableLambda, RunnablePassthrough
from langchain_core.runnables.history import RunnableWithMessageHistory
from langchain_google_genai import ChatGoogleGenerativeAI
from pydantic import BaseModel, Field, create_model

from .config import settings

# Every domain named here must have an entry in contacts.json (checked by test_data_files.py)
DOMAIN_ROUTING_RULES = (
    "- If the user asks about visas, CAS, or moving to the UK -> use 'international_students'.\n"
    "- If the user asks about Wi-Fi, passwords, laptops, software, login issues, or Eduroam -> use 'it_support'.\n"
    "- If the user asks about gp, doctors, isolation, mental health, or counseling -> use 'health_wellbeing'.\n"
    "- If the user asks about library hours, studying, or assignments -> use 'academic_info'.\n"
    "- If the user asks about Welcome Week, freshers, induction, fees, or events -> use 'new_students'.\n"
)

# One call writes the search query and picks the department, instead of a rewrite call followed by a
# self-query call: each Gemini call costs 10-20 s on the free tier.
SEARCH_SYSTEM_PROMPT = (
    "You turn a Heriot-Watt student's latest question into a search over the university's official pages. "
    "Read the chat history and the latest question, then fill in both fields.\n"
    "query:\n"
    "1. IF NEW TOPIC: If the latest question is about a brand new topic, IGNORE the chat history completely and use the question itself.\n"
    "2. IF CONTINUATION: If the user uses pronouns (it, they, them) or asks a follow-up (e.g., 'tell me more', 'where is that?'), use the chat history to figure out what they are referring to and write a complete standalone query.\n"
    "3. EDINBURGH DEFAULT: Always add 'Edinburgh campus' to the query unless Dubai or Malaysia is explicitly mentioned.\n"
    "4. TRANSLATE: If the question is not in English, write the query in English.\n"
    "domain: the department whose pages answer the question. CRITICAL ROUTING RULES:\n"
    + DOMAIN_ROUTING_RULES
    + "If you are unsure, do NOT guess a domain. Leave it empty."
)

RETRIEVAL_KWARGS = {
    "k": 4,  # final number of chunks sent to the LLM
    "fetch_k": 40,  # initial pool of chunks fetched before filtering
    "lambda_mult": 0.5,  # diversity vs. relevance
}


def _search_model(domains: list[str]) -> type[BaseModel]:
    # The allowed domains come from contacts.json, so the filter and the hand-off directory can't drift
    # apart, and the schema stops the model from naming a department that doesn't exist
    return create_model(
        "Search",
        query=(str, Field(description="A standalone search query in English.")),
        domain=(
            Literal[tuple(domains)] | None,
            Field(default=None, description="The department whose pages answer the question, or null if unsure."),
        ),
    )

SYSTEM_PROMPT = (
    "You are a strict and helpful assistant for Heriot-Watt students. "
    "CRITICAL INSTRUCTIONS:\n"
    "0. CONVERSATIONAL FLOW: Read the chat history. If the user replies with an agreement (e.g., 'yes', 'please', 'list them') to your previous response, you MUST fulfill that offer using the provided Context.\n"
    "1. SAFEGUARDING & WELLBEING (CRITICAL): If the user expresses feelings of isolation, loneliness, depression, or emotional distress, you MUST prioritize their safety. You must express brief, warm empathy, and immediately urge them to contact the Student Wellbeing team at {wellbeing_email}. Do this regardless of what the Context says.\n"
    "2. ELABORATION: If the user asks for more details, provide NEW details from the Context that you haven't mentioned yet.\n"
    "3. You MUST answer the user's question using ONLY the information found in the Context. Do NOT use outside knowledge, guess, or make assumptions.\n"
    "4. OFF-TOPIC REJECTION (CRITICAL): If the user's question is clearly NOT about Heriot-Watt University (e.g., pancake recipes, coding help, general trivia, movies), you MUST politely decline to answer. You must NOT provide any contact emails. Do this even if there is text in the Context.\n"
    "5. MISSING INFORMATION & HANDOFF: If the question IS about the university, but the provided Context does NOT contain the answer (or the Context is completely empty), you must state exactly: 'I cannot find the exact answer to that in my current official university files.' THEN, use the Contact Directory below to provide the best matching email address.\n"
    "6. LINK FORMATTING: Format all provided URLs as clickable Markdown links.\n"
    "7. You MUST reply in the EXACT SAME LANGUAGE as the user's original question.\n"
    "8. SOURCES: The Context is split into numbered sources ([Source 1], [Source 2], ...). In source_ids, list the numbers "
    "of the sources your answer is actually based on. Leave it empty if you declined, handed off, or did not use the Context. "
    "Never mention source numbers in the answer text itself.\n"
    "\nContact Directory: {contacts}\n"
    "Context:\n{context}"
)


class RagAnswer(BaseModel):
    answer: str = Field(description="The reply shown to the user, formatted in Markdown.")
    source_ids: list[int] = Field(
        description=(
            "Numbers of the Context sources the answer is based on. "
            "Empty if you declined, handed off to a contact, or did not use the Context."
        )
    )


def _format_docs(docs: list[Document]) -> str:
    return "\n\n".join(f"[Source {i}]\n{doc.page_content}" for i, doc in enumerate(docs, start=1))


def build_conversational_chain(
    vectorstore: Chroma,
    contacts: dict,
    get_session_history: Callable[[str], BaseChatMessageHistory],
) -> Runnable:
    llm = ChatGoogleGenerativeAI(
        model=settings.llm_model,
        temperature=0.0,
        google_api_key=settings.google_api_key,
    )

    search_prompt = ChatPromptTemplate.from_messages(
        [
            ("system", SEARCH_SYSTEM_PROMPT),
            MessagesPlaceholder("chat_history"),
            ("human", "{input}"),
        ]
    )

    def search(params: BaseModel) -> list[Document]:
        # MMR keeps the chunks diverse, so four chunks from one long page don't crowd out the rest
        return vectorstore.max_marginal_relevance_search(
            params.query,
            filter={"domain": params.domain} if params.domain else None,
            **RETRIEVAL_KWARGS,
        )

    retriever = search_prompt | llm.with_structured_output(_search_model(list(contacts))) | RunnableLambda(search)

    qa_prompt = ChatPromptTemplate.from_messages(
        [
            ("system", SYSTEM_PROMPT),
            MessagesPlaceholder("chat_history"),
            ("human", "{input}"),
        ]
    ).partial(contacts=json.dumps(contacts), wellbeing_email=contacts["health_wellbeing"])

    # Structured output instead of free text, so the backend never has to parse
    # tags out of the model's reply. The retrieved docs stay in "context";
    # only the prompt sees them as numbered text.
    question_answer_chain = (
        RunnablePassthrough.assign(context=lambda x: _format_docs(x["context"]))
        | qa_prompt
        | llm.with_structured_output(RagAnswer)
    )
    rag_chain = (
        RunnablePassthrough.assign(context=retriever)
        .assign(result=question_answer_chain)
        .assign(answer=lambda x: x["result"].answer)
    )

    return RunnableWithMessageHistory(
        rag_chain,
        get_session_history,
        input_messages_key="input",
        history_messages_key="chat_history",
        output_messages_key="answer",
    )

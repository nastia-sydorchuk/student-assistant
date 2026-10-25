import json
from typing import Callable

from langchain_chroma import Chroma
from langchain_classic.chains import create_history_aware_retriever, create_retrieval_chain
from langchain_classic.chains.combine_documents import create_stuff_documents_chain
from langchain_classic.chains.query_constructor.base import AttributeInfo
from langchain_classic.retrievers.self_query.base import SelfQueryRetriever
from langchain_core.chat_history import BaseChatMessageHistory
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain_core.runnables import Runnable
from langchain_core.runnables.history import RunnableWithMessageHistory
from langchain_google_genai import ChatGoogleGenerativeAI

from .config import settings

METADATA_FIELD_INFO = [
    AttributeInfo(
        name="domain",
        description=(
            "The specific university department this document belongs to. "
            "You MUST restrict your output to ONLY one of these exact strings: "
            "['it_support', 'health_wellbeing', 'international_students', 'academic_info', 'campus_info', 'new_students']. "
            "CRITICAL ROUTING RULES: "
            "- If the user asks about visas, CAS, or moving to the UK -> use 'international_students'. "
            "- If the user asks about Wi-Fi, passwords, laptops, software, login issues, or Eduroam -> use 'it_support'. "
            "- If the user asks about gp, doctors, isolation, mental health, or counseling -> use 'health_wellbeing'. "
            "- If the user asks about library hours, studying, or assignments -> use 'academic_info'. "
            "- If the user asks about Welcome Week, freshers, induction, fees, or events -> use 'new_students'. "
            "If you are unsure, do NOT guess a domain. Leave the filter blank."
        ),
        type="string",
    )
]

DOCUMENT_CONTENT_DESCRIPTION = "Official guides and resources for Heriot-Watt University students."

CONTEXTUALIZE_Q_SYSTEM_PROMPT = (
    "You are an expert search query generator. Your ONLY job is to output a search string. "
    "Read the chat history and the latest user question, then follow these rules STRICTLY:\n"
    "1. IF NEW TOPIC: If the user's latest question is about a brand new topic, IGNORE the chat history completely. Just return the user's new question as the search query.\n"
    "2. IF CONTINUATION: If the user uses pronouns (it, they, them) or asks a follow-up (e.g., 'tell me more', 'where is that?'), use the chat history to figure out what they are referring to and write a complete standalone query.\n"
    "3. EDINBURGH DEFAULT: Always add 'Edinburgh campus' to the search query unless Dubai or Malaysia is explicitly mentioned.\n"
    "4. TRANSLATE: If the question is not in English, translate the search query to English.\n"
    "5. OUTPUT: Return ONLY the raw search query. No conversational text, no quotes, no explanations."
)

SYSTEM_PROMPT = (
    "You are a strict and helpful assistant for Heriot-Watt students. "
    "CRITICAL INSTRUCTIONS:\n"
    "0. CONVERSATIONAL FLOW: Read the chat history. If the user replies with an agreement (e.g., 'yes', 'please', 'list them') to your previous response, you MUST fulfill that offer using the provided Context.\n"
    "1. SAFEGUARDING & WELLBEING (CRITICAL): If the user expresses feelings of isolation, loneliness, depression, or emotional distress, you MUST prioritize their safety. You must express brief, warm empathy, and immediately urge them to contact the Student Wellbeing team at studentwellbeing@hw.ac.uk. Do this regardless of what the Context says.\n"
    "2. ELABORATION: If the user asks for more details, provide NEW details from the Context that you haven't mentioned yet.\n"
    "3. You MUST answer the user's question using ONLY the information found in the Context. Do NOT use outside knowledge, guess, or make assumptions.\n"
    "4. OFF-TOPIC REJECTION (CRITICAL): If the user's question is clearly NOT about Heriot-Watt University (e.g., pancake recipes, coding help, general trivia, movies), you MUST politely decline to answer. You must NOT provide any contact emails. Do this even if there is text in the Context.\n"
    "5. MISSING INFORMATION & HANDOFF: If the question IS about the university, but the provided Context does NOT contain the answer (or the Context is completely empty), you must state exactly: 'I cannot find the exact answer to that in my current official university files.' THEN, use the Contact Directory below to provide the best matching email address.\n"
    "6. LINK FORMATTING: Format all provided URLs as clickable Markdown links.\n"
    "7. You MUST reply in the EXACT SAME LANGUAGE as the user's original question.\n"
    "8. At the very end of your response, on a new line, output exactly one of these tags:\n"
    "   - [SOURCES_USED] if your answer was based on the Context.\n"
    "   - [NO_SOURCES] if you declined, handed off, or did not use the Context.\n"
    "\nContact Directory: {contacts}\n"
    "Context: {context}"
)


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

    retriever = SelfQueryRetriever.from_llm(
        llm=llm,
        vectorstore=vectorstore,
        document_contents=DOCUMENT_CONTENT_DESCRIPTION,
        metadata_field_info=METADATA_FIELD_INFO,
        search_type="mmr",
        search_kwargs={
            "k": 4,  # final number of chunks sent to the LLM
            "fetch_k": 40,  # initial pool of chunks fetched before filtering
            "lambda_mult": 0.5,  # diversity vs. relevance
        },
    )

    contextualize_q_prompt = ChatPromptTemplate.from_messages(
        [
            ("system", CONTEXTUALIZE_Q_SYSTEM_PROMPT),
            MessagesPlaceholder("chat_history"),
            ("human", "{input}"),
        ]
    )
    history_aware_retriever = create_history_aware_retriever(llm, retriever, contextualize_q_prompt)

    qa_prompt = ChatPromptTemplate.from_messages(
        [
            ("system", SYSTEM_PROMPT),
            MessagesPlaceholder("chat_history"),
            ("human", "{input}"),
        ]
    ).partial(contacts=json.dumps(contacts))

    question_answer_chain = create_stuff_documents_chain(llm, qa_prompt)
    rag_chain = create_retrieval_chain(history_aware_retriever, question_answer_chain)

    return RunnableWithMessageHistory(
        rag_chain,
        get_session_history,
        input_messages_key="input",
        history_messages_key="chat_history",
        output_messages_key="answer",
    )

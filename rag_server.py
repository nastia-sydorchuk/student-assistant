import os
import time
import html2text
import nest_asyncio
from bs4 import BeautifulSoup, SoupStrainer
from fastapi import FastAPI
from pydantic import BaseModel
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_chroma import Chroma
from langchain_classic.chains import create_retrieval_chain
from langchain_classic.chains.combine_documents import create_stuff_documents_chain
from langchain_core.prompts import ChatPromptTemplate
from langchain_community.document_loaders import PyPDFLoader, SitemapLoader, TextLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_community.document_loaders import WebBaseLoader
from langchain_huggingface import HuggingFaceEmbeddings
from langchain_core.prompts import MessagesPlaceholder
from langchain_classic.chains import create_history_aware_retriever
from langchain_core.runnables.history import RunnableWithMessageHistory
from langchain_community.chat_message_histories import ChatMessageHistory
from langchain_core.messages import AIMessage
from langchain_core.chat_history import InMemoryChatMessageHistory
import os
import json
from dotenv import load_dotenv

from langchain_classic.chains.query_constructor.base import AttributeInfo
from langchain_classic.retrievers.self_query.base import SelfQueryRetriever
from langchain_community.document_loaders import AsyncHtmlLoader
from urllib.parse import urljoin

import json

# Load the dynamic contact directory
with open("contacts.json", "r") as f:
    CONTACT_DIRECTORY = json.dumps(json.load(f))

load_dotenv()
nest_asyncio.apply()

app = FastAPI()

# --- 1. SOURCE MANAGER & METADATA INJECTION ---
class SourceManager:
    def __init__(self, config_path):
        with open(config_path, 'r') as file:
            self.config = json.load(file)

    def load_and_tag_documents(self):
        all_docs = []
        
        md_converter = html2text.HTML2Text()
        md_converter.ignore_links = False
        md_converter.ignore_images = True
        
        for source in self.config.get('sources', []):
            if source['type'] == 'web':
                print(f"Loading raw HTML from {source['name']}...")

                loader = AsyncHtmlLoader(web_path=source.get('urls', []))
                raw_docs = loader.load()
                
                for doc in raw_docs:
                    current_url = doc.metadata.get('source', '')
                    
                    soup = BeautifulSoup(doc.page_content, "html.parser")
                    
                    for a_tag in soup.find_all('a', href=True):
                        a_tag['href'] = urljoin(current_url, a_tag['href'])
                    
                    main_content = soup.find(["main", "article"])
                    
                    if main_content:
                        doc.page_content = md_converter.handle(str(main_content))
                    else:
                        doc.page_content = md_converter.handle(str(soup))
                    doc.metadata.update(source.get('metadata', {}))
                    doc.metadata['source_name'] = source.get('name', 'Unknown')
                    
                    all_docs.append(doc)
                    
        return all_docs

# --- 2. INGESTION PIPELINE ---
manager = SourceManager('sources.json')
embeddings = HuggingFaceEmbeddings(model_name="sentence-transformers/all-MiniLM-L6-v2")

if not os.path.exists("./chroma_db"):
    print("Loading sources from JSON and injecting metadata...")
    all_docs = manager.load_and_tag_documents()
    
    text_splitter = RecursiveCharacterTextSplitter(
        chunk_size=6000,
        chunk_overlap=500,
        separators=["\n\n", "\n", " ", ""]
    )
    splits = text_splitter.split_documents(all_docs)
    
    print(f"Building local database from {len(splits)} chunks...")
    vectorstore = Chroma.from_documents(
        documents=splits, 
        embedding=embeddings, 
        persist_directory="./chroma_db"
    )
    print("Database built instantly!")
else:
    print("Loading existing database from disk...")
    vectorstore = Chroma(persist_directory="./chroma_db", embedding_function=embeddings)


# --- 3. SELF-QUERY RETRIEVER SETUP ---
llm = ChatGoogleGenerativeAI(model="gemini-2.5-flash-lite", temperature=0.0)

metadata_field_info = [
    AttributeInfo(
        name="domain",
        description=(
            "The specific department or topic the user is asking about. "
            "You MUST choose exactly one of these valid values based on the query: "
            "1. 'it_support' (Use for: WiFi, Eduroam, passwords, laptops, software, login issues) "
            "2. 'health_wellbeing' (Use for: doctors, GP, mental health, counseling, illness, disability) "
            "3. 'new_students' (Use for: Welcome Week, freshers, induction, fees, events) "
            "4. 'academic_info' (Use for: studying, library, exams, revision, writing, study spaces, academic skills) "
            "5. 'campus_info' (Use for: general campus location, maps, finding buildings, general student life). "
            "6. 'international_students' (Use for: visas, immigration, BRP, moving to the UK, global student office, culture shock, general UK information, council, societies) "
            "7. SAFEGUARDING & WELLBEING (CRITICAL): If the user expresses feelings of isolation, loneliness, depression, or emotional distress, you MUST prioritize their safety. You must express brief, warm empathy, and immediately urge them to contact the Student Wellbeing team at studentwellbeing@hw.ac.uk or visit the campus wellbeing desk. Do this regardless of what the Context says.\n"
            "If you are unsure, default to 'campus_info'."
        ),
        type="string",
    )
]

document_content_description = "Official guides and resources for Heriot-Watt University students."
# document_content_description = "Information for Heriot-Watt University students, including academic skills, wellbeing,student services, campuses, and IT support."
retriever = SelfQueryRetriever.from_llm(
    llm=llm,
    vectorstore=vectorstore,
    document_contents=document_content_description,
    metadata_field_info=metadata_field_info,
    search_type="similarity_score_threshold",
    search_kwargs={
        "score_threshold": 0.3,
    }
)

# retriever = vectorstore.as_retriever()
# --- 4. RAG CHAIN SETUP ---
chat_store = {}

def get_session_history(session_id: str):
    if session_id not in chat_store:
        chat_store[session_id] = InMemoryChatMessageHistory() # Updated to use InMemoryChatMessageHistory
    return chat_store[session_id]

contextualize_q_prompt = ChatPromptTemplate.from_messages([
    ("system", 
     "You are an expert search query generator. Your ONLY job is to output a search string. "
     "Read the chat history and the latest user question, then follow these rules STRICTLY:\n"
     "1. IF NEW TOPIC: If the user's latest question is about a brand new topic, IGNORE the chat history completely. Just return the user's new question as the search query.\n"
     "2. IF CONTINUATION: If the user uses pronouns (it, they, them) or asks a follow-up (e.g., 'tell me more', 'where is that?'), use the chat history to figure out what they are referring to and write a complete standalone query.\n"
     "3. EDINBURGH DEFAULT: Always add 'Edinburgh campus' to the search query unless Dubai or Malaysia is explicitly mentioned.\n"
     "4. TRANSLATE: If the question is not in English, translate the search query to English.\n"
     "5. OUTPUT: Return ONLY the raw search query. No conversational text, no quotes, no explanations."
    ),
    MessagesPlaceholder("chat_history"),
    ("human", "{input}"),
])

history_aware_retriever = create_history_aware_retriever(llm, retriever, contextualize_q_prompt)

system_prompt = (
    "You are a strict and helpful assistant for Heriot-Watt students. "
    "CRITICAL INSTRUCTIONS:\n"
    "0. CONVERSATIONAL FLOW: Read the chat history. If the user replies with an agreement (e.g., 'yes', 'please', 'list them') to your previous response, you MUST fulfill that offer using the provided Context.\n"
    "1. ELABORATION: If the user asks for more details, provide NEW details from the Context that you haven't mentioned yet.\n"
    "2. You MUST answer the user's question using ONLY the information found in the provided Context.\n"
    "3. Do NOT use outside knowledge, guess, or make assumptions.\n"
    "4. OFF-TOPIC REJECTION: If the provided Context is completely empty and the question is NOT about the university, politely decline to answer.\n"
    "5. GRACEFUL HANDOFF: If the user asks a university-related question but the Context is empty, state: 'I cannot find the exact answer to that in my current official university files.' Then, determine the topic of their question, find the matching department in the Contact Directory below, and provide them with that exact email address.\n"
    "6. LINK FORMATTING: Format all provided URLs as clickable Markdown links.\n"
    "7. You MUST reply in the EXACT SAME LANGUAGE as the user's original question.\n"

    "\nContact Directory: {contacts}\n"
    "Context: {context}"
)

qa_prompt = ChatPromptTemplate.from_messages([
    ("system", system_prompt),
    MessagesPlaceholder("chat_history"),
    ("human", "{input}")
])

question_answer_chain = create_stuff_documents_chain(llm, qa_prompt)
rag_chain = create_retrieval_chain(history_aware_retriever, question_answer_chain)

conversational_rag_chain = RunnableWithMessageHistory(
    rag_chain,
    get_session_history,
    input_messages_key="input",
    history_messages_key="chat_history",
    output_messages_key="answer",
)

# --- 5. ENDPOINTS ---
class QueryRequest(BaseModel):
    query: str
    current_url: str = "Unknown"
    session_id: str = "default_session"
    bot_greeting: str = None

@app.post("/rag-query")
async def query_rag(request: QueryRequest):
    history = get_session_history(request.session_id)

    if len(history.messages) > 4:
        history.messages = history.messages[-4:]
    
    if len(history.messages) == 0 and request.bot_greeting:
        history.add_message(AIMessage(content=request.bot_greeting))

    response = conversational_rag_chain.invoke(
        {
            "input": request.query,
            "current_url": request.current_url
        },
        config={"configurable": {"session_id": request.session_id}}
    )

    retrieved_docs = response.get("context", [])
    print(f"Database returned {len(retrieved_docs)} results.")
    
    answer = response["answer"]
    sources = set()

    for doc in response["context"]:
        source_name = doc.metadata.get("source", "Unknown Source")
        
        page_num = doc.metadata.get("page")
        
        if page_num is not None:
            source_name = f"{source_name} (Page {page_num + 1})"
            
        sources.add(source_name)
        
    return {
        "answer": answer,
        "sources": list(sources)
    }

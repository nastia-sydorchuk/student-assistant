import os
import time
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
# from langchain_community.chat_models import ChatOllama
from langchain_core.messages import AIMessage
import os
from dotenv import load_dotenv

load_dotenv()
nest_asyncio.apply()

app = FastAPI()

sitemap_url = "https://www.hw.ac.uk/sitemap.xml"

# 1. Defining all sources
sources = [
    # PyPDFLoader("https://www.hw.ac.uk/document-library/students/FOA-Student-Guide.pdf"),
    SitemapLoader(
        web_path=sitemap_url,
        filter_urls=["https://www.hw.ac.uk/students/", "https://www.hw.ac.uk/campuses/edinburgh/", "https://hw.ac.libcal.com/calendar/studentevents", "http://hw.ac.uk/about/professional-services/information-services", "https://www.hw.ac.uk/study/how-to-apply/academic-calendar"],
        bs_kwargs={
            "parse_only": SoupStrainer(["main", "article"])
        }
    ),
    # WebBaseLoader(
    #     web_path=["https://www.hw.ac.uk"],
    #     bs_kwargs={"parse_only": SoupStrainer(["main", "article"])}
    # ),
]

# 2. Collecting all documents
all_docs = []
for loader in sources:
    all_docs.extend(loader.load())

# 3. Splitting into chunks
text_splitter = RecursiveCharacterTextSplitter(
    chunk_size=6000,      # High capacity per request
    chunk_overlap=500,    # Enough context between chunks
    separators=["\n\n", "\n", " ", ""]
)
splits = text_splitter.split_documents(all_docs)

# 4. Setup Embeddings
# embeddings = GoogleGenerativeAIEmbeddings(model="models/gemini-embedding-001")
# embeddings = HuggingFaceEmbeddings(model_name="sentence-transformers/all-MiniLM-L6-v2")
embeddings = HuggingFaceEmbeddings(model_name="sentence-transformers/all-MiniLM-L6-v2")
# embeddings = HuggingFaceEmbeddings(model_name="paraphrase-multilingual-MiniLM-L12-v2")

# 5. Build/Load Database with Rate Limiting
if not os.path.exists("./chroma_db"):
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

retriever = vectorstore.as_retriever(
    # search_type="similarity_score_threshold",
    search_kwargs={
        # "score_threshold": 0.4, # docs with a >40% match
        # "k": 6                  
    }
)

chat_store = {}

def get_session_history(session_id: str):
    if session_id not in chat_store:
        chat_store[session_id] = ChatMessageHistory()
    return chat_store[session_id]

# 3. Setup the Chain
llm = ChatGoogleGenerativeAI(model="gemini-2.5-flash-lite", temperature=0.0)

contextualize_q_prompt = ChatPromptTemplate.from_messages([
    ("system", "Given a chat history and the latest user question, formulate a standalone search query IN ENGLISH that can be used to search a database. "
               "If the user asks a continuation question (e.g., 'tell me more', 'expand on that', 'what are they'), look at the IMMEDIATELY PRECEDING user topic in the chat history. Formulate the search based on THAT topic. "
               "DEFAULT TO EDINBURGH: Always assume they are asking about the Edinburgh campus unless Dubai or Malaysia is explicitly mentioned. Add 'Edinburgh campus' to the search query."
               "If the user's question is in Spanish (or any other language), you MUST translate it to English for the search query. "
               "Do NOT answer the question, just return the translated/reformulated English search query."),
    MessagesPlaceholder("chat_history"),
    ("human", "{input}"),
])

history_aware_retriever = create_history_aware_retriever(llm, retriever, contextualize_q_prompt)

system_prompt = (
    "You are a strict and helpful assistant for Heriot-Watt students. "
    "CRITICAL INSTRUCTIONS:\n"
    "0. CONVERSATIONAL FLOW: Read the chat history. If the user replies with an agreement (e.g., 'yes', 'please', 'list them') to your previous response, you MUST fulfill that offer using the provided Context.\n"
    "1. ELABORATION ('Tell me more'): If the user asks for more details, review your previous answer and provide NEW, specific details from the Context that you haven't mentioned yet. If there are no new details in the Context, politely reply: 'That is all the specific information I have available on this topic right now.' Do NOT use the standard missing info error.\n"
    "2. You MUST answer the user's question using ONLY the information found in the provided Context.\n"
    "3. Do NOT use outside knowledge, guess, or make assumptions.\n"
    "4. For new questions, if the exact answer is not in the Context, you must reply: 'I cannot find the answer to that in the provided official university information.'\n"
    "5. You MUST reply in the EXACT SAME LANGUAGE as the user's original question (e.g., if the user asks in Spanish, translate your final grounded answer into Spanish).\n"
    # "6. DEFAULT TO EDINBURGH: You MUST assume the user is asking about the Edinburgh campus unless they explicitly specify otherwise in their question.\n"
    "\nContext: {context}"
)

# --- 2. THE MAIN ANSWER PROMPT ---
qa_prompt = ChatPromptTemplate.from_messages([
    ("system", system_prompt),
    MessagesPlaceholder("chat_history"),
    ("human", "{input}")
])
question_answer_chain = create_stuff_documents_chain(llm, qa_prompt)
rag_chain = create_retrieval_chain(history_aware_retriever, question_answer_chain)

# --- 3. WRAP IN THE MEMORY MANAGER ---
conversational_rag_chain = RunnableWithMessageHistory(
    rag_chain,
    get_session_history,
    input_messages_key="input",
    history_messages_key="chat_history",
    output_messages_key="answer",
)

# --- ENDPOINTS ---

class QueryRequest(BaseModel):
    query: str
    current_url: str = "Unknown"
    session_id: str = "default_session"
    bot_greeting: str = None

@app.post("/rag-query")
async def query_rag(request: QueryRequest):
    history = get_session_history(request.session_id)
    
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

# To run: uvicorn rag_server:app --reload --port 8000
export interface ChatRequest {
  query: string;
  session_id: string;
  bot_greeting: string;
}

export interface ChatResponse {
  answer: string;
  sources: string[];
}

export interface ChatMessage {
  text: string;
  isUser: boolean;
  sources?: { url: string; label: string }[]; 
}
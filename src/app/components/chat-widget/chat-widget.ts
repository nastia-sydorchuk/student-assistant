import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { filter, finalize } from 'rxjs/operators';
import { MarkdownPipe } from '../../pipes/markdown-pipe';
import { Api } from '../../services/api';
import { ChatMessage, ChatRequest } from '../../models/chat.model';
import { NavigationEnd, NavigationStart, Router } from '@angular/router';

@Component({
  selector: 'app-chat-widget',
  imports: [CommonModule, FormsModule, MarkdownPipe],
  templateUrl: './chat-widget.html',
  styleUrl: './chat-widget.css',
})
export class ChatWidget {
  isOpen = false;
  userInput = '';
  isLoading = false;
  private openTimer: any;

  sessionId = 'session_' + Math.random().toString(36).substring(7);

  messages: ChatMessage[] = [
    // { text: 'Hello! I can answer questions about the Heriot-Watt Student Guide in English or Spanish. How can I help?', isUser: false }
  ];

  private pageGreetings: { [key: string]: string } = {
    // Main Dashboard
    '/': 'Welcome back to your Student Portal! Can I help you find any specific resources today?',
    
    // Core Navigation Pages
    '/academic': 'Hello! Do you need help finding library resources, study spaces, or academic skills support?',
    '/facilities': 'Hi there! Want to know more about our facilities or how to get here?',
    '/services': 'Welcome! Need assistance with IT support, student wellbeing, accommodation, or your career plans?',
    '/cultural': 'Hi! Want to know more about the Student Union, societies, sports clubs, or volunteering opportunities?',
    
    // Onboarding & Events
    '/registration': 'Hello! Do you have any questions about your enrollment checklist, courses selection, or paying tuition fees?',
    '/induction': 'Welcome! Are you looking for specific welcome events, fairs, or orientation schedules?',
    
    '/induction/undergraduate': 'Hi! Ready to start your undergraduate journey? Ask me anything about the Welcome Week or eligibility for the success events!',
    '/induction/postgraduate': 'Welcome to your postgraduate studies! Need help finding your specific networking events or academic timetable?',
    
    'default': 'Hi! How can I help you with your Heriot-Watt queries today?'
  };

  currentGreeting: string = '';

  constructor(private api: Api, private cdr: ChangeDetectorRef, private router: Router) {}

  ngOnInit() {
    this.router.events.subscribe((event: any) => {
      this.setGreetingAndOpen(this.router.url);
      if (event instanceof NavigationStart && this.messages.length <= 1) {
        this.isOpen = false;
        if (this.openTimer) {
          clearTimeout(this.openTimer);
        }

        this.cdr.detectChanges();
      }

      if (event instanceof NavigationEnd) {
        this.setGreetingAndOpen(event.urlAfterRedirects || '');
      }
    });
  }

  private setGreetingAndOpen(url: string) {
    let greeting = this.pageGreetings[url] ||this.pageGreetings['default'];

    if (this.messages.length === 0 || (this.messages.length === 1 && !this.messages[0].isUser)) {
      this.messages = [{ text: greeting, isUser: false }];
    }

    if (this.openTimer) clearTimeout(this.openTimer);
    
    this.openTimer = setTimeout(() => {
      this.isOpen = true;
      this.cdr.detectChanges(); 
    }, 4000);
  }

  toggleChat() {
    this.isOpen = !this.isOpen;
  }

  // Helper to format the raw source strings into clean clickable objects
  private formatSources(rawSources: string[]): { url: string; label: string }[] {
    return rawSources.map(sourceStr => {
      const parts = sourceStr.split(' (Page ');
      const url = parts[0];
      const isPdf = url.toLowerCase().includes('.pdf');
      
      let label = isPdf ? 'PDF Document' : 'Web Page';
      
      if (parts.length > 1) {
        const pageNum = parts[1].replace(')', '');
        label += ` (Page ${pageNum})`;
      }
      
      return { url, label };
    });
  }

  sendMessage() {
    if (!this.userInput.trim()) return;

    const currentGreeting = this.messages.length > 0 ? this.messages[0].text : '';

    const userText = this.userInput.trim();
    this.messages.push({ text: userText, isUser: true });
    this.userInput = '';
    
    this.isLoading = true;
    this.scrollToBottom();

    const requestPayload: ChatRequest = {
      query: userText,
      current_url: window.location.href,
      session_id: this.sessionId,
      bot_greeting: currentGreeting
    };

    this.api.sendMessage(requestPayload).pipe(
      finalize(() => {
        this.isLoading = false;
        this.cdr.detectChanges();
        this.scrollToBottom();
      })
    )
    .subscribe({
      next: (response) => {
        this.messages.push({
          text: response.answer,
          isUser: false,
          sources: response.sources ? this.formatSources(response.sources) : []
        });
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.messages.push({ 
          text: 'Sorry, I could not connect to the server.', 
          isUser: false 
        });
        this.cdr.detectChanges();
      }
    });
  }

  // Helper to keep the chat scrolled to the newest message
  private scrollToBottom() {
    setTimeout(() => {
      const chatContainer = document.getElementById('chat-history');
      if (chatContainer) {
        chatContainer.scrollTop = chatContainer.scrollHeight;
      }
    }, 100);
  }
}

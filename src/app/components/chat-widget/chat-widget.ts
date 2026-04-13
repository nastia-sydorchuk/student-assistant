import { ChangeDetectorRef, Component, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { filter, finalize } from 'rxjs/operators';
import { MarkdownPipe } from '../../pipes/markdown-pipe';
import { Api } from '../../services/api';
import { ChatMessage, ChatRequest } from '../../models/chat.model';
import { NavigationEnd, NavigationStart, Router } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-chat-widget',
  imports: [CommonModule, FormsModule, MarkdownPipe, TranslateModule],
  templateUrl: './chat-widget.html',
  styleUrl: './chat-widget.css',
})
export class ChatWidget {
  isOpen = false;
  userInput = '';
  isLoading = false;
  private openTimer: any;

  sessionId = 'session_' + Math.random().toString(36).substring(7);

  messages: ChatMessage[] = [];

  private pageGreetings: { [key: string]: string } = {
    '/': 'GREETINGS.HOME',
    '/academic': 'GREETINGS.ACADEMIC',
    '/facilities': 'GREETINGS.FACILITIES',
    '/services': 'GREETINGS.SERVICES',
    '/cultural': 'GREETINGS.CULTURAL',
    '/registration': 'GREETINGS.REGISTRATION',
    '/induction': 'GREETINGS.INDUCTION',
    '/induction/undergraduate': 'GREETINGS.INDUCTION_UNDERGRADUATE',
    '/induction/postgraduate': 'GREETINGS.INDUCTION_POSTGRADUATE',
    'default': 'GREETINGS.DEFAULT'
  };

  currentGreeting: string = '';

  constructor(private api: Api, private cdr: ChangeDetectorRef, private router: Router, @Inject(PLATFORM_ID) private platformId: Object, private translateService: TranslateService) {}

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
    const key = this.pageGreetings[url] ||this.pageGreetings['default'];
    const greetingText = this.translateService.instant(key);

    if (this.messages.length === 0 || (this.messages.length === 1 && !this.messages[0].isUser)) {
      this.messages = [{ text: greetingText, isUser: false }];
    }

    if (this.openTimer) clearTimeout(this.openTimer);

    let userClosedChat = false; 

    if (isPlatformBrowser(this.platformId)) {
      userClosedChat = sessionStorage.getItem('chatClosedManually') === 'true';
    }
    
    if (!userClosedChat) {
        this.openTimer = setTimeout(() => {
          this.isOpen = true;
          this.cdr.detectChanges();
        }, 2000);
    }
  }

  toggleChat() {
    this.isOpen = !this.isOpen;
    
    if (isPlatformBrowser(this.platformId)) {
      if (!this.isOpen) {
        sessionStorage.setItem('chatClosedManually', 'true');
      } else {
        sessionStorage.removeItem('chatClosedManually');
      }
    }
  }

  // Helper to format the raw source strings into clean clickable objects
  private formatSources(rawSources: string[]): { url: string; label: string }[] {
    return rawSources.map((urlString: string) => {
      try {
        const url = new URL(urlString);
        const pathSegments = url.pathname.split('/').filter(Boolean);
        
        let formattedText = "";

        if (pathSegments.length === 0) {
          const domainName = url.hostname.replace(/^www\./, '').split('.')[0];
          formattedText = domainName.charAt(0).toUpperCase() + domainName.slice(1);
        } else {
          const targetSegments = pathSegments.slice(-2);
          
          formattedText = targetSegments.map(segment => {
            return segment
              .replace(/[-_]/g, ' ')
              .split(' ')
              .map((word: string) => word.charAt(0).toUpperCase() + word.slice(1))
              .join(' ');
          }).join(' - ');
        }

        return {
          url: urlString,
          label: formattedText
        };
        
      } catch (error) {
        // Safe fallback if the string is not a valid URL
        return {
          url: urlString,
          label: "External Link"
        };
      }
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

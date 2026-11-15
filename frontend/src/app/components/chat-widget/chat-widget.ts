import { Component, DestroyRef, ElementRef, Injector, OnInit, PLATFORM_ID, afterNextRender, inject, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { MarkdownPipe } from '../../pipes/markdown-pipe';
import { ApiService } from '../../services/api.service';
import { ChatMessage, ChatRequest } from '../../models/chat.model';
import { Event as RouterEvent, NavigationEnd, NavigationStart, Router } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-chat-widget',
  imports: [CommonModule, FormsModule, MarkdownPipe, TranslateModule],
  templateUrl: './chat-widget.html',
  styleUrl: './chat-widget.css',
  // Escape from anywhere in the widget (input, buttons, history) closes the chat
  host: { '(keydown.escape)': 'closeChat()' },
})
export class ChatWidget implements OnInit {
  private apiService = inject(ApiService);
  private router = inject(Router);
  private platformId = inject(PLATFORM_ID);
  private translateService = inject(TranslateService);
  private destroyRef = inject(DestroyRef);
  private injector = inject(Injector);

  private toggleButton = viewChild<ElementRef<HTMLButtonElement>>('toggleButton');
  private chatInput = viewChild<ElementRef<HTMLInputElement>>('chatInput');

  isOpen = signal(false);
  isLoading = signal(false);
  messages = signal<ChatMessage[]>([]);
  // Read out by the visually hidden live region in the template
  announcement = signal('');

  userInput = '';
  readonly maxInputLength = 1000;
  private openTimer: ReturnType<typeof setTimeout> | undefined;

  // Must be a bare UUID: the RAG service rejects any other format.
  sessionId = crypto.randomUUID();

  private pageGreetings: Record<string, string> = {
    '/': 'GREETINGS.HOME',
    '/home': 'GREETINGS.HOME',
    '/academic': 'GREETINGS.ACADEMIC',
    '/wellbeing': 'GREETINGS.WELLBEING',
    '/facilities': 'GREETINGS.FACILITIES',
    '/services': 'GREETINGS.SERVICES',
    '/cultural': 'GREETINGS.CULTURAL',
    '/registration': 'GREETINGS.REGISTRATION',
    '/induction': 'GREETINGS.INDUCTION',
    '/induction/undergraduate': 'GREETINGS.INDUCTION_UNDERGRADUATE',
    '/induction/postgraduate': 'GREETINGS.INDUCTION_POSTGRADUATE',
    'default': 'GREETINGS.DEFAULT'
  };

  ngOnInit(): void {
    this.setGreetingAndOpen(this.router.url);

    this.router.events
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((event: RouterEvent) => {
        if (event instanceof NavigationStart && this.messages().length <= 1) {
          this.isOpen.set(false);
          if (this.openTimer) {
            clearTimeout(this.openTimer);
          }
        }

        if (event instanceof NavigationEnd) {
          this.setGreetingAndOpen(event.urlAfterRedirects || '');
        }
      });

    this.translateService.onLangChange
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.setGreetingAndOpen(this.router.url));
  }

  private setGreetingAndOpen(url: string) {
    const key = this.pageGreetings[url] || this.pageGreetings['default'];
    const greetingText = this.translateService.instant(key);

    const currentMessages = this.messages();
    if (currentMessages.length === 0 || (currentMessages.length === 1 && !currentMessages[0].isUser)) {
      this.messages.set([{ text: greetingText, isUser: false }]);
    }

    if (this.openTimer) clearTimeout(this.openTimer);

    let userClosedChat = false;

    if (isPlatformBrowser(this.platformId)) {
      userClosedChat = sessionStorage.getItem('chatClosedManually') === 'true';
    }

    if (!userClosedChat) {
      this.openTimer = setTimeout(() => {
        this.isOpen.set(true);
      }, 2000);
    }
  }

  toggleChat() {
    if (this.isOpen()) {
      this.closeChat();
      return;
    }

    this.isOpen.set(true);
    if (isPlatformBrowser(this.platformId)) {
      sessionStorage.removeItem('chatClosedManually');
    }
    // Only when the user opens it: the automatic open after 2s must not take focus away from the page
    this.focusAfterRender(() => this.chatInput());
  }

  closeChat() {
    if (!this.isOpen()) return;

    this.isOpen.set(false);
    if (isPlatformBrowser(this.platformId)) {
      sessionStorage.setItem('chatClosedManually', 'true');
    }
    // The close button or input that had focus is gone now, so hand focus back to the toggle
    this.focusAfterRender(() => this.toggleButton());
  }

  private focusAfterRender(target: () => ElementRef<HTMLElement> | undefined) {
    afterNextRender(() => target()?.nativeElement.focus(), { injector: this.injector });
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
        
      } catch {
        // Safe fallback if the string is not a valid URL
        return {
          url: urlString,
          label: this.translateService.instant('CHAT.EXTERNAL_LINK')
        };
      }
    });
  }

  sendMessage() {
    // The input stays enabled while loading so it keeps focus; block a second send here instead
    if (!this.userInput.trim() || this.isLoading()) return;

    const currentMessages = this.messages();
    const currentGreeting = currentMessages.length > 0 ? currentMessages[0].text : '';

    const userText = this.userInput.trim().slice(0, this.maxInputLength);
    this.messages.update(msgs => [...msgs, { text: userText, isUser: true }]);
    this.userInput = '';

    this.isLoading.set(true);
    this.announcement.set(this.translateService.instant('CHAT.THINKING'));
    this.scrollToBottom();
    // After clicking Send, go back to the input so the next question can be typed straight away
    this.chatInput()?.nativeElement.focus();

    const requestPayload: ChatRequest = {
      query: userText,
      session_id: this.sessionId,
      bot_greeting: currentGreeting
    };

    this.apiService.sendMessage(requestPayload).pipe(
      finalize(() => {
        this.isLoading.set(false);
        this.scrollToBottom();
      })
    )
    .subscribe({
      next: (response) => {
        this.announcement.set(response.answer);
        this.messages.update(msgs => [...msgs, {
          text: response.answer,
          isUser: false,
          sources: response.sources ? this.formatSources(response.sources) : []
        }]);
      },
      error: () => {
        const errorText = this.translateService.instant('CHAT.CONNECTION_ERROR');
        this.announcement.set(errorText);
        this.messages.update(msgs => [...msgs, {
          text: errorText,
          isUser: false
        }]);
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

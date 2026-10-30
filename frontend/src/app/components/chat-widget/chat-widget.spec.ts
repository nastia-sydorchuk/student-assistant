import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';

import { ChatWidget } from './chat-widget';
import { ApiService } from '../../services/api.service';

@Component({ selector: 'app-blank-test', template: '' })
class BlankTestComponent {}

describe('ChatWidget', () => {
  let component: ChatWidget;
  let fixture: ComponentFixture<ChatWidget>;
  let apiService: { sendMessage: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    apiService = { sendMessage: vi.fn() };
    sessionStorage.clear();

    await TestBed.configureTestingModule({
      imports: [ChatWidget],
      providers: [
        provideRouter([
          { path: 'academic', component: BlankTestComponent },
          { path: 'wellbeing', component: BlankTestComponent },
        ]),
        provideTranslateService(),
        { provide: ApiService, useValue: apiService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ChatWidget);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('does nothing when the input is blank', () => {
    component.userInput = '   ';
    component.sendMessage();

    expect(apiService.sendMessage).not.toHaveBeenCalled();
    expect(component.messages()).toEqual([{ text: 'GREETINGS.HOME', isUser: false }]);
  });

  it('appends the reply and formats sources on a successful send', () => {
    apiService.sendMessage.mockReturnValue(
      of({ answer: 'Here you go', sources: ['https://example.com/campus/map'] }),
    );

    component.userInput = 'Where is the library?';
    component.sendMessage();

    expect(component.isLoading()).toBe(false);
    // messages[0] is the page greeting
    const messages = component.messages();
    expect(messages[1]).toEqual({ text: 'Where is the library?', isUser: true });
    expect(messages[2].text).toBe('Here you go');
    expect(messages[2].sources).toEqual([
      { url: 'https://example.com/campus/map', label: 'Campus - Map' },
    ]);
    expect(component.userInput).toBe('');
  });

  it('sends the same bare UUID as session_id on every message', () => {
    apiService.sendMessage.mockReturnValue(of({ answer: 'ok', sources: [] }));

    component.userInput = 'first';
    component.sendMessage();
    component.userInput = 'second';
    component.sendMessage();

    const [first, second] = apiService.sendMessage.mock.calls.map(([payload]) => payload.session_id);
    expect(first).toMatch(/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/);
    expect(second).toBe(first);
  });

  it('shows a fallback message when the request fails', () => {
    apiService.sendMessage.mockReturnValue(throwError(() => new Error('network down')));

    component.userInput = 'hello';
    component.sendMessage();

    expect(component.isLoading()).toBe(false);
    const messages = component.messages();
    expect(messages[messages.length - 1]).toEqual({
      text: 'CHAT.CONNECTION_ERROR',
      isUser: false,
    });
  });

  it('toggles open state and remembers manual close via sessionStorage', () => {
    expect(component.isOpen()).toBe(false);

    component.toggleChat();
    expect(component.isOpen()).toBe(true);
    expect(sessionStorage.getItem('chatClosedManually')).toBeNull();

    component.toggleChat();
    expect(component.isOpen()).toBe(false);
    expect(sessionStorage.getItem('chatClosedManually')).toBe('true');
  });

  it('greets the user for the current route and auto-opens after navigating', async () => {
    vi.useFakeTimers();
    try {
      const router = TestBed.inject(Router);
      fixture.detectChanges();

      await router.navigateByUrl('/academic');

      expect(component.messages()[0]?.text).toBe('GREETINGS.ACADEMIC');
      expect(component.isOpen()).toBe(false);

      vi.advanceTimersByTime(2000);

      expect(component.isOpen()).toBe(true);
    } finally {
      vi.useRealTimers();
    }
  });

  it('greets the user on the page the app opened on', () => {
    fixture.detectChanges();

    expect(component.messages()).toEqual([{ text: 'GREETINGS.HOME', isUser: false }]);
  });

  it('has a greeting for the wellbeing page', async () => {
    fixture.detectChanges();

    await TestBed.inject(Router).navigateByUrl('/wellbeing');

    expect(component.messages()[0]?.text).toBe('GREETINGS.WELLBEING');
  });
});

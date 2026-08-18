import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApiService } from './api.service';
import { ChatRequest } from '../models/chat.model';
import { LevelData } from '../models/welcome.model';

describe('ApiService', () => {
  let service: ApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(ApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('fetches onboarding steps', () => {
    const mockSteps = [{ order: 1, summary: 's', description: 'd', url: 'u', actionText: 'a' }];

    service.getOnboardingSteps().subscribe((steps) => {
      expect(steps).toEqual(mockSteps);
    });

    const req = httpMock.expectOne('/api/onboarding/steps');
    expect(req.request.method).toBe('GET');
    req.flush(mockSteps);
  });

  it('fetches induction events', () => {
    const mockEvents = [{ title: 't', icon: 'i', desc: 'd', actionText: 'a', url: 'u' }];

    service.getInductionEvents().subscribe((events) => {
      expect(events).toEqual(mockEvents);
    });

    const req = httpMock.expectOne('/api/onboarding/induction-events');
    expect(req.request.method).toBe('GET');
    req.flush(mockEvents);
  });

  it('fetches resources for a category', () => {
    const mockCategory = { id: 'academic', categoryTitle: 't', description: 'd', items: [] };

    service.getResources('academic').subscribe((category) => {
      expect(category).toEqual(mockCategory);
    });

    const req = httpMock.expectOne('/api/resources/academic');
    expect(req.request.method).toBe('GET');
    req.flush(mockCategory);
  });

  it('fetches onboarding config by level', () => {
    const mockLevel: LevelData = {
      title: 't',
      level: 'postgraduate',
      importantDates: [],
      event: {
        title: 'et',
        description: 'ed',
        programmeOutline: [],
        registration: { label: 'l', link: 'link' },
      },
    };

    service.getOnboardingConfigByLevel('postgraduate').subscribe((level) => {
      expect(level).toEqual(mockLevel);
    });

    const req = httpMock.expectOne('/api/onboarding/postgraduate');
    expect(req.request.method).toBe('GET');
    req.flush(mockLevel);
  });

  it('posts a chat message', () => {
    const request: ChatRequest = {
      query: 'hello',
      current_url: 'http://localhost/',
      session_id: 'session_abc',
      bot_greeting: 'hi',
    };
    const mockResponse = { answer: 'answer', sources: ['https://example.com'] };

    service.sendMessage(request).subscribe((response) => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne('/api/gemini/chat');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });

  it('fetches portal cards', () => {
    const mockCards = [{ id: '1', title: 't', route: '/r', icon: 'i', description: 'd' }];

    service.getPortalCards().subscribe((cards) => {
      expect(cards).toEqual(mockCards);
    });

    const req = httpMock.expectOne('/api/portal-cards');
    expect(req.request.method).toBe('GET');
    req.flush(mockCards);
  });
});

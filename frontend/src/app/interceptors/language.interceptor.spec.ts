import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { languageInterceptor } from './language.interceptor';
import { LanguageService } from '../services/language.service';

describe('languageInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([languageInterceptor])),
        provideHttpClientTesting(),
        { provide: LanguageService, useValue: { getCurrentLanguage: () => 'es' } },
      ],
    });

    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('adds the Accept-Language header to /api/ requests', () => {
    httpClient.get('/api/onboarding/steps').subscribe();

    const req = httpMock.expectOne('/api/onboarding/steps');
    expect(req.request.headers.get('Accept-Language')).toBe('es');
    req.flush({});
  });

  it('leaves requests outside /api/ untouched', () => {
    httpClient.get('/assets/en.json').subscribe();

    const req = httpMock.expectOne('/assets/en.json');
    expect(req.request.headers.has('Accept-Language')).toBe(false);
    req.flush({});
  });
});

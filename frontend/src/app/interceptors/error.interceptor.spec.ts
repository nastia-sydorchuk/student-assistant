import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { errorInterceptor } from './error.interceptor';
import { LoggerService } from '../services/logger.service';

describe('errorInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  let logger: { error: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    logger = { error: vi.fn() };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
        { provide: LoggerService, useValue: logger },
      ],
    });

    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('logs and rethrows on a failed request', () => {
    let caughtError: unknown;

    httpClient.get('/api/onboarding/steps').subscribe({
      error: (err) => (caughtError = err),
    });

    const req = httpMock.expectOne('/api/onboarding/steps');
    req.flush('boom', { status: 500, statusText: 'Server Error' });

    expect(logger.error).toHaveBeenCalledWith(
      'Request failed: GET /api/onboarding/steps',
      expect.anything(),
    );
    expect(caughtError).toBeTruthy();
  });

  it('does not log on a successful request', () => {
    httpClient.get('/api/onboarding/steps').subscribe();

    const req = httpMock.expectOne('/api/onboarding/steps');
    req.flush([]);

    expect(logger.error).not.toHaveBeenCalled();
  });
});

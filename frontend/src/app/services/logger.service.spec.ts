import { TestBed } from '@angular/core/testing';
import { LoggerService } from './logger.service';

describe('LoggerService', () => {
  let service: LoggerService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LoggerService);
  });

  afterEach(() => {
    vi.restoreAllMocks();
    vi.unstubAllGlobals();
  });

  it('delegates log to console.log in dev mode', () => {
    const spy = vi.spyOn(console, 'log').mockImplementation(() => undefined);
    service.log('hello', 1, 2);
    expect(spy).toHaveBeenCalledWith('hello', 1, 2);
  });

  it('delegates warn to console.warn in dev mode', () => {
    const spy = vi.spyOn(console, 'warn').mockImplementation(() => undefined);
    service.warn('careful', { id: 1 });
    expect(spy).toHaveBeenCalledWith('careful', { id: 1 });
  });

  it('always delegates error to console.error', () => {
    const spy = vi.spyOn(console, 'error').mockImplementation(() => undefined);
    const err = new Error('boom');
    service.error('failed', err);
    expect(spy).toHaveBeenCalledWith('failed', err);
  });

  // isDevMode() reads the ngDevMode global, which production builds set to false
  it('suppresses log and warn in production', () => {
    vi.stubGlobal('ngDevMode', false);
    const logSpy = vi.spyOn(console, 'log').mockImplementation(() => undefined);
    const warnSpy = vi.spyOn(console, 'warn').mockImplementation(() => undefined);

    service.log('hello');
    service.warn('careful');

    expect(logSpy).not.toHaveBeenCalled();
    expect(warnSpy).not.toHaveBeenCalled();
  });

  it('still delegates error to console.error in production', () => {
    vi.stubGlobal('ngDevMode', false);
    const spy = vi.spyOn(console, 'error').mockImplementation(() => undefined);
    service.error('failed');
    expect(spy).toHaveBeenCalledWith('failed');
  });
});

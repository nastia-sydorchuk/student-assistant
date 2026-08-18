import { TestBed } from '@angular/core/testing';
import { LoggerService } from './logger.service';

describe('LoggerService', () => {
  let service: LoggerService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LoggerService);
  });

  it('delegates log to console.log', () => {
    const spy = vi.spyOn(console, 'log').mockImplementation(() => undefined);
    service.log('hello', 1, 2);
    expect(spy).toHaveBeenCalledWith('hello', 1, 2);
  });

  it('delegates warn to console.warn', () => {
    const spy = vi.spyOn(console, 'warn').mockImplementation(() => undefined);
    service.warn('careful', { id: 1 });
    expect(spy).toHaveBeenCalledWith('careful', { id: 1 });
  });

  it('delegates error to console.error', () => {
    const spy = vi.spyOn(console, 'error').mockImplementation(() => undefined);
    const err = new Error('boom');
    service.error('failed', err);
    expect(spy).toHaveBeenCalledWith('failed', err);
  });
});

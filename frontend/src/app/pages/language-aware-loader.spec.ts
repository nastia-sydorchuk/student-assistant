import { TestBed } from '@angular/core/testing';
import { BehaviorSubject, Subject, of, throwError } from 'rxjs';

import { LanguageAwareLoader, combinedState } from './language-aware-loader';
import { LanguageService } from '../services/language.service';

describe('LanguageAwareLoader', () => {
  let currentLanguage$: BehaviorSubject<string>;

  beforeEach(() => {
    currentLanguage$ = new BehaviorSubject<string>('en');
    TestBed.configureTestingModule({
      providers: [{ provide: LanguageService, useValue: { currentLanguage$ } }],
    });
  });

  function createLoader<T>(initialValue: T, errorMessage = 'failed'): LanguageAwareLoader<T> {
    return TestBed.runInInjectionContext(() => new LanguageAwareLoader<T>(initialValue, errorMessage));
  }

  it('starts with the initial value and loading', () => {
    const loader = createLoader<string[]>([]);

    expect(loader.data()).toEqual([]);
    expect(loader.isLoading()).toBe(true);
    expect(loader.error()).toBe('');
  });

  it('loads data and stops loading', () => {
    const loader = createLoader<string | null>(null);

    loader.load(() => of('hello'));

    expect(loader.data()).toBe('hello');
    expect(loader.isLoading()).toBe(false);
  });

  it('reloads when the language changes', () => {
    const loader = createLoader<string | null>(null);
    const request = vi.fn(() => of(currentLanguage$.getValue()));

    loader.load(request);
    currentLanguage$.next('es');

    expect(request).toHaveBeenCalledTimes(2);
    expect(loader.data()).toBe('es');
  });

  it('ignores a slow answer for a language the user has already switched away from', () => {
    const loader = createLoader<string | null>(null);
    const english = new Subject<string>();
    const spanish = new Subject<string>();
    loader.load(() => (currentLanguage$.getValue() === 'en' ? english : spanish));

    currentLanguage$.next('es');
    spanish.next('Hola');
    english.next('Hello');

    expect(loader.data()).toBe('Hola');
  });

  it('sets the error message and stops loading when a request fails', () => {
    const loader = createLoader<string | null>(null, 'Could not load');

    loader.load(() => throwError(() => new Error('boom')));

    expect(loader.error()).toBe('Could not load');
    expect(loader.isLoading()).toBe(false);
  });

  it('clears an earlier error once a reload succeeds', () => {
    const loader = createLoader<string | null>(null);
    let fail = true;
    loader.load(() => (fail ? throwError(() => new Error('boom')) : of('ok')));

    fail = false;
    currentLanguage$.next('es');

    expect(loader.error()).toBe('');
    expect(loader.data()).toBe('ok');
  });

  it('reloads with each new param value', () => {
    const loader = createLoader<string | null>(null);
    const level$ = new BehaviorSubject('undergraduate');

    loader.load((level: string) => of(`${level}:${currentLanguage$.getValue()}`), level$);
    level$.next('postgraduate');

    expect(loader.data()).toBe('postgraduate:en');
  });

  describe('combinedState', () => {
    it('keeps loading until every loader is done, and reports the first error', () => {
      const first = createLoader<string | null>(null, 'first failed');
      const second = createLoader<string | null>(null, 'second failed');
      const pending = new Subject<string>();
      const state = combinedState(first, second);

      first.load(() => throwError(() => new Error('boom')));
      second.load(() => pending);

      expect(state.isLoading()).toBe(true);
      expect(state.error()).toBe('first failed');

      pending.next('done');

      expect(state.isLoading()).toBe(false);
    });
  });
});

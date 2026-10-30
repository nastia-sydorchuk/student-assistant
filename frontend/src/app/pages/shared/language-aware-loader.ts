import { DestroyRef, Signal, WritableSignal, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { EMPTY, Observable, catchError, combineLatest, of, switchMap, tap } from 'rxjs';
import { LanguageService } from '../../services/language.service';

// Loads one piece of page data and reloads it whenever the language changes (the API answers
// in the language of the request).
export class LanguageAwareLoader<T> {
  private languageService = inject(LanguageService);
  private destroyRef = inject(DestroyRef);

  private readonly value: WritableSignal<T>;
  private readonly loading = signal(true);
  private readonly failure = signal('');

  readonly data: Signal<T>;
  readonly isLoading = this.loading.asReadonly();
  readonly error = this.failure.asReadonly();

  // errorMessage is what error() holds after a failed load; empty for pages that show no error
  constructor(initialValue: T, private readonly errorMessage = '') {
    this.value = signal(initialValue);
    this.data = this.value.asReadonly();
  }

  // params$ adds another reason to reload, e.g. a route parameter; each value is passed to request
  load<P = void>(request: (params: P) => Observable<T>, params$: Observable<P> = of(undefined as P)): void {
    combineLatest([params$, this.languageService.currentLanguage$])
      .pipe(
        tap(() => {
          this.loading.set(true);
          this.failure.set('');
        }),
        // switchMap drops a request still in flight, so a slow answer in the previous
        // language can't overwrite the new one
        switchMap(([params]) =>
          request(params).pipe(
            catchError(() => {
              this.failure.set(this.errorMessage);
              this.loading.set(false);
              return EMPTY;
            }),
          ),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((value) => {
        this.value.set(value);
        this.loading.set(false);
      });
  }
}

// Combined state for a page that loads several things: loading until all are done, first error wins
export function combinedState(...loaders: LanguageAwareLoader<unknown>[]) {
  return {
    isLoading: computed(() => loaders.some((loader) => loader.isLoading())),
    error: computed(() => loaders.map((loader) => loader.error()).find(Boolean) ?? ''),
  };
}

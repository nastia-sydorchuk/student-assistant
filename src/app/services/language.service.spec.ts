import { TestBed } from '@angular/core/testing';
import { provideTranslateService, TranslateService } from '@ngx-translate/core';
import { LanguageService } from './language.service';

describe('LanguageService', () => {
  let translate: TranslateService;
  let useSpy: ReturnType<typeof vi.spyOn>;
  let fallbackSpy: ReturnType<typeof vi.spyOn>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideTranslateService()],
    });

    translate = TestBed.inject(TranslateService);
    useSpy = vi.spyOn(translate, 'use');
    fallbackSpy = vi.spyOn(translate, 'setFallbackLang');
  });

  it('sets the fallback language and defaults to English on construction', () => {
    const service = TestBed.inject(LanguageService);

    expect(fallbackSpy).toHaveBeenCalledWith('en');
    expect(useSpy).toHaveBeenCalledWith('en');
    expect(service.getCurrentLanguage()).toBe('en');
  });

  it('changes the current language and delegates to TranslateService', () => {
    const service = TestBed.inject(LanguageService);
    useSpy.mockClear();

    service.changeLanguage('es');

    expect(service.getCurrentLanguage()).toBe('es');
    expect(useSpy).toHaveBeenCalledWith('es');
  });

  it('emits the current language on currentLanguage$', () => {
    const service = TestBed.inject(LanguageService);
    const emitted: string[] = [];
    service.currentLanguage$.subscribe((lang) => emitted.push(lang));

    service.changeLanguage('es');

    expect(emitted).toEqual(['en', 'es']);
  });
});

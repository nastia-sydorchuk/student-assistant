import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { TranslateService } from '@ngx-translate/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class LanguageService {
  private translate = inject(TranslateService);
  private platformId = inject(PLATFORM_ID);

  private languageSubject = new BehaviorSubject<string>('en');

  public currentLanguage$ = this.languageSubject.asObservable();

  constructor() {
    this.translate.setFallbackLang('en');

    let savedLang = 'en';
    if (isPlatformBrowser(this.platformId)) {
      savedLang = localStorage.getItem('lang') ?? 'en';
    }

    this.languageSubject.next(savedLang);
    this.translate.use(savedLang);
  }

  changeLanguage(lang: string): void {
    this.languageSubject.next(lang);
    this.translate.use(lang);

    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem('lang', lang);
    }
  }

  getCurrentLanguage(): string {
    return this.languageSubject.getValue();
  }
}

import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class LanguageService {
  private languageSubject = new BehaviorSubject<string>('en');
  
  public currentLanguage$ = this.languageSubject.asObservable();

  constructor(private translate: TranslateService) {
    this.translate.setFallbackLang('en');
    const savedLang = 'en';
    this.languageSubject.next(savedLang);
    this.translate.use(savedLang);
  }

  changeLanguage(lang: string): void {
    this.languageSubject.next(lang);
    this.translate.use(lang);
  }

  getCurrentLanguage(): string {
    return this.languageSubject.getValue();
  }
}

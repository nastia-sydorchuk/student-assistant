import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideTranslateService } from '@ngx-translate/core';
import { App } from './app';
import { LanguageService } from './services/language.service';

describe('App', () => {
  let languageService: LanguageService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideTranslateService(),
      ],
    }).compileComponents();

    languageService = TestBed.inject(LanguageService);
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render the navbar brand', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.navbar-brand')).toBeTruthy();
  });

  it('should default currentLang to the language service value', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app.currentLang).toBe(languageService.getCurrentLanguage());
  });

  it('should render languages from the API', () => {
    const httpMock = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    httpMock.expectOne('/api/languages').flush([
      { code: 'en', name: 'English' },
      { code: 'fr', name: 'Français' },
    ]);
    fixture.detectChanges();

    const options = (fixture.nativeElement as HTMLElement).querySelectorAll('select option');
    expect(Array.from(options).map((o) => o.textContent?.trim())).toEqual(['English', 'Français']);
  });

  it('should hide the language select until languages load', () => {
    const httpMock = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).querySelector('select')).toBeNull();

    httpMock.expectOne('/api/languages').flush('boom', { status: 500, statusText: 'Server Error' });
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).querySelector('select')).toBeNull();
  });

  it('should delegate onLanguageChange to the language service', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    const changeLanguageSpy = vi.spyOn(languageService, 'changeLanguage');

    const select = document.createElement('select');
    const option = document.createElement('option');
    option.value = 'es';
    select.appendChild(option);
    select.value = 'es';

    app.onLanguageChange({ target: select } as unknown as Event);

    expect(changeLanguageSpy).toHaveBeenCalledWith('es');
  });
});

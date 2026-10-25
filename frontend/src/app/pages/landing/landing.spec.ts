import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { BehaviorSubject, of, throwError } from 'rxjs';

import { Landing } from './landing';
import { ApiService } from '../../services/api.service';
import { LanguageService } from '../../services/language.service';
import { PortalCard } from '../../models/portal-card.model';

describe('Landing', () => {
  let component: Landing;
  let fixture: ComponentFixture<Landing>;
  let apiService: { getPortalCards: ReturnType<typeof vi.fn> };
  let currentLanguage$: BehaviorSubject<string>;

  const mockCards: PortalCard[] = [
    { id: '1', title: 'Academic', route: '/academic', icon: 'i', description: 'd' },
  ];

  beforeEach(async () => {
    apiService = { getPortalCards: vi.fn().mockReturnValue(of(mockCards)) };
    currentLanguage$ = new BehaviorSubject<string>('en');

    await TestBed.configureTestingModule({
      imports: [Landing],
      providers: [
        provideRouter([]),
        provideTranslateService(),
        { provide: ApiService, useValue: apiService },
        { provide: LanguageService, useValue: { currentLanguage$ } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Landing);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads portal cards on init', () => {
    fixture.detectChanges();

    expect(apiService.getPortalCards).toHaveBeenCalled();
    expect(component.portalCards()).toEqual(mockCards);
    expect(component.isLoading()).toBe(false);
  });

  it('reloads portal cards whenever the language changes', () => {
    fixture.detectChanges();
    apiService.getPortalCards.mockClear();

    currentLanguage$.next('es');

    expect(apiService.getPortalCards).toHaveBeenCalled();
  });

  it('stops loading when the request fails', () => {
    apiService.getPortalCards.mockReturnValue(throwError(() => new Error('boom')));

    fixture.detectChanges();

    expect(component.isLoading()).toBe(false);
    expect(component.portalCards()).toEqual([]);
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { BehaviorSubject, of, throwError } from 'rxjs';

import { Induction } from './induction';
import { ApiService } from '../../services/api.service';
import { LanguageService } from '../../services/language.service';
import { InductionEvent } from '../../models/welcome.model';

describe('Induction', () => {
  let component: Induction;
  let fixture: ComponentFixture<Induction>;
  let apiService: { getInductionEvents: ReturnType<typeof vi.fn> };
  let currentLanguage$: BehaviorSubject<string>;

  const mockEvents: InductionEvent[] = [
    { title: 'Welcome Talk', icon: 'i', desc: 'd', actionText: 'Go', url: '/x' },
  ];

  beforeEach(async () => {
    apiService = { getInductionEvents: vi.fn().mockReturnValue(of(mockEvents)) };
    currentLanguage$ = new BehaviorSubject<string>('en');

    await TestBed.configureTestingModule({
      imports: [Induction],
      providers: [
        provideRouter([]),
        provideTranslateService(),
        { provide: ApiService, useValue: apiService },
        { provide: LanguageService, useValue: { currentLanguage$ } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Induction);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads induction events on init', () => {
    fixture.detectChanges();

    expect(apiService.getInductionEvents).toHaveBeenCalled();
    expect(component.inductionEvents()).toEqual(mockEvents);
    expect(component.isLoadingEvents()).toBe(false);
  });

  it('reloads events whenever the language changes', () => {
    fixture.detectChanges();
    apiService.getInductionEvents.mockClear();

    currentLanguage$.next('es');

    expect(apiService.getInductionEvents).toHaveBeenCalled();
  });

  it('surfaces an error message when the request fails', () => {
    apiService.getInductionEvents.mockReturnValue(throwError(() => new Error('boom')));

    fixture.detectChanges();

    expect(component.isLoadingEvents()).toBe(false);
    expect(component.eventsError()).toBe(
      'Unable to load induction events at this time. Please try again later.',
    );
  });
});

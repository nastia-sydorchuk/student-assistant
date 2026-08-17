import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { BehaviorSubject, of, throwError } from 'rxjs';

import { Registration } from './registration';
import { ApiService } from '../../services/api.service';
import { LanguageService } from '../../services/language.service';
import { Step } from '../../models/welcome.model';
import { ResourceCategory } from '../../models/resources.model';

describe('Registration', () => {
  let component: Registration;
  let fixture: ComponentFixture<Registration>;
  let apiService: { getResources: ReturnType<typeof vi.fn>; getOnboardingSteps: ReturnType<typeof vi.fn> };
  let currentLanguage$: BehaviorSubject<string>;

  const mockSteps: Step[] = [
    { order: 1, summary: 's', description: 'd', url: 'u', actionText: 'a' },
  ];
  const mockResources: ResourceCategory = {
    id: 'tech',
    categoryTitle: 't',
    description: 'd',
    items: [],
  };

  beforeEach(async () => {
    apiService = {
      getResources: vi.fn().mockReturnValue(of(mockResources)),
      getOnboardingSteps: vi.fn().mockReturnValue(of(mockSteps)),
    };
    currentLanguage$ = new BehaviorSubject<string>('en');

    await TestBed.configureTestingModule({
      imports: [Registration],
      providers: [
        provideRouter([]),
        provideTranslateService(),
        { provide: ApiService, useValue: apiService },
        { provide: LanguageService, useValue: { currentLanguage$ } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Registration);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads tech resources and onboarding steps on init', () => {
    fixture.detectChanges();

    expect(apiService.getResources).toHaveBeenCalledWith('tech');
    expect(apiService.getOnboardingSteps).toHaveBeenCalled();
    expect(component.techResources()).toEqual(mockResources);
    expect(component.onboardingSteps()).toEqual(mockSteps);
    expect(component.isLoading()).toBe(false);
  });

  it('reloads data whenever the language changes', () => {
    fixture.detectChanges();
    apiService.getResources.mockClear();
    apiService.getOnboardingSteps.mockClear();

    currentLanguage$.next('es');

    expect(apiService.getResources).toHaveBeenCalledWith('tech');
    expect(apiService.getOnboardingSteps).toHaveBeenCalled();
  });

  it('surfaces an error message when resources fail to load', () => {
    apiService.getResources.mockReturnValue(throwError(() => new Error('boom')));

    fixture.detectChanges();

    expect(component.errorMessage()).toBe(
      'Could not load resources at this time. Please try again later.',
    );
  });

  it('surfaces an error message when onboarding steps fail to load', () => {
    apiService.getOnboardingSteps.mockReturnValue(throwError(() => new Error('boom')));

    fixture.detectChanges();

    expect(component.errorMessage()).toBe(
      'Unable to load onboarding steps at this time. Please try again later.',
    );
  });
});

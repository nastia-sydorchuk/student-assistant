import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { BehaviorSubject, of, throwError } from 'rxjs';

import { PostgradInduction } from './postgrad-induction';
import { ApiService } from '../../services/api.service';
import { LevelData } from '../../models/welcome.model';

describe('PostgradInduction', () => {
  let component: PostgradInduction;
  let fixture: ComponentFixture<PostgradInduction>;
  let apiService: { getOnboardingConfigByLevel: ReturnType<typeof vi.fn> };
  let paramMap$: BehaviorSubject<ReturnType<typeof convertToParamMap>>;

  const mockLevelData: LevelData = {
    title: 't',
    level: 'postgraduate',
    importantDates: [],
    event: {
      title: 'Welcome Event',
      description: 'd',
      programmeOutline: [],
      registration: { label: 'Register', link: 'https://example.com/register' },
    },
  };

  beforeEach(async () => {
    apiService = { getOnboardingConfigByLevel: vi.fn().mockReturnValue(of(mockLevelData)) };
    paramMap$ = new BehaviorSubject(convertToParamMap({ level: 'postgraduate' }));

    await TestBed.configureTestingModule({
      imports: [PostgradInduction],
      providers: [
        provideRouter([]),
        provideTranslateService(),
        { provide: ApiService, useValue: apiService },
        { provide: ActivatedRoute, useValue: { paramMap: paramMap$ } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PostgradInduction);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads induction data for the level in the route', () => {
    fixture.detectChanges();

    expect(apiService.getOnboardingConfigByLevel).toHaveBeenCalledWith('postgraduate');
    expect(component.pageData()).toEqual(mockLevelData);
    expect(component.isLoading()).toBe(false);
  });

  it('reloads when the route level param changes', () => {
    fixture.detectChanges();
    apiService.getOnboardingConfigByLevel.mockClear();

    paramMap$.next(convertToParamMap({ level: 'undergraduate' }));

    expect(apiService.getOnboardingConfigByLevel).toHaveBeenCalledWith('undergraduate');
  });

  it('surfaces an error message when the request fails', () => {
    apiService.getOnboardingConfigByLevel.mockReturnValue(throwError(() => new Error('boom')));

    fixture.detectChanges();

    expect(component.isLoading()).toBe(false);
    expect(component.errorMessage()).toBe('Could not load the welcome guide. Please try again later.');
  });

  it('maps badge types to their CSS classes', () => {
    fixture.detectChanges();

    expect(component.getBadgeClass('action')).toBe('bg-primary');
    expect(component.getBadgeClass('deadline')).toBe('bg-danger');
    expect(component.getBadgeClass('event')).toBe('bg-success');
    expect(component.getBadgeClass('other')).toBe('bg-secondary');
  });
});

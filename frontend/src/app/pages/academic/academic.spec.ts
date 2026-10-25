import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { BehaviorSubject, of, throwError } from 'rxjs';

import { Academic } from './academic';
import { ApiService } from '../../services/api.service';
import { LanguageService } from '../../services/language.service';
import { ResourceCategory } from '../../models/resources.model';

describe('Academic', () => {
  let component: Academic;
  let fixture: ComponentFixture<Academic>;
  let apiService: { getResources: ReturnType<typeof vi.fn> };
  let currentLanguage$: BehaviorSubject<string>;

  beforeEach(async () => {
    apiService = { getResources: vi.fn().mockReturnValue(of({ id: 'academic' } as ResourceCategory)) };
    currentLanguage$ = new BehaviorSubject<string>('en');

    await TestBed.configureTestingModule({
      imports: [Academic],
      providers: [
        provideRouter([]),
        provideTranslateService(),
        { provide: ApiService, useValue: apiService },
        { provide: LanguageService, useValue: { currentLanguage$ } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Academic);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads resources for the academic category on init', () => {
    fixture.detectChanges();

    expect(apiService.getResources).toHaveBeenCalledWith('academic');
    expect(component.resources()).toEqual({ id: 'academic' });
    expect(component.isLoading()).toBe(false);
  });

  it('reloads resources whenever the language changes', () => {
    fixture.detectChanges();
    apiService.getResources.mockClear();

    currentLanguage$.next('es');

    expect(apiService.getResources).toHaveBeenCalledWith('academic');
  });

  it('surfaces an error message when the request fails', () => {
    apiService.getResources.mockReturnValue(throwError(() => new Error('boom')));

    fixture.detectChanges();

    expect(component.isLoading()).toBe(false);
    expect(component.errorMessage()).toBe(
      'Could not load resources at this time. Please try again later.',
    );
  });
});

import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Step } from '../../models/welcome.model';
import { ApiService } from '../../services/api.service';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ResourceCategory } from '../../models/resources.model';
import { LanguageService } from '../../services/language.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-registration',
  imports: [RouterLink, CommonModule, TranslateModule],
  templateUrl: './registration.html',
  styleUrl: './registration.css',
})
export class Registration {
  private apiService = inject(ApiService);
  private languageService = inject(LanguageService);
  private destroyRef = inject(DestroyRef);

  onboardingSteps = signal<Step[] | null>(null);
  techResources = signal<ResourceCategory | null>(null);

  isLoading = signal(true);
  errorMessage = signal('');

  ngOnInit(): void {
    this.languageService.currentLanguage$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.loadCategory('tech');
        this.fetchInductionSteps();
      });
  }

  loadCategory(categoryId: string): void {
    this.isLoading.set(true);

    this.apiService.getResources(categoryId).subscribe({
      next: (data) => {
        this.techResources.set(data);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error fetching resources:', error);
        this.errorMessage.set('Could not load resources at this time. Please try again later.');
        this.isLoading.set(false);
      }
    });
  }

  fetchInductionSteps(): void {
    this.apiService.getOnboardingSteps().subscribe({
      next: (data) => {
        this.onboardingSteps.set(data);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error fetching events:', error);
        this.errorMessage.set('Unable to load onboarding steps at this time. Please try again later.');
        this.isLoading.set(false);
      }
    });
  }
}

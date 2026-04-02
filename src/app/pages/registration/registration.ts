import { ChangeDetectorRef, Component } from '@angular/core';
import { Step, WelcomePage } from '../../models/welcome.model';
import { Api } from '../../services/api';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ResourceCategory } from '../../models/resources.model';
import { LanguageService } from '../../services/language.service';

@Component({
  selector: 'app-registration',
  imports: [RouterLink, CommonModule],
  templateUrl: './registration.html',
  styleUrl: './registration.css',
})
export class Registration {
  onboardingSteps: Step[] | null = null;
  techResources: ResourceCategory | null = null;
    
  isLoading = true;
  errorMessage = '';

  constructor(private resourceService: Api, private cdr: ChangeDetectorRef, private languageService: LanguageService) {}

  ngOnInit(): void {
    this.fetchInductionSteps();
    this.languageService.currentLanguage$.subscribe(newLang => {
      this.loadCategory('tech', newLang);
    });
  }

  loadCategory(categoryId: string, languageCode: string): void {
    this.isLoading = true;
    
    this.resourceService.getResources(categoryId, languageCode).subscribe({
      next: (data) => {
        this.techResources = data;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error fetching resources:', error);
        this.errorMessage = 'Could not load resources at this time. Please try again later.';
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  fetchInductionSteps() {
    this.resourceService.getOnboardingSteps().subscribe({
      next: (data) => {
        this.onboardingSteps = data;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error fetching events:', error);
        this.errorMessage = 'Unable to load onboarding steps at this time. Please try again later.';
        this.isLoading = false;
      }
    });
  }
}

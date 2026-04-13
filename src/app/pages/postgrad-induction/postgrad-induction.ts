import { ChangeDetectorRef, Component } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { LevelData } from '../../models/welcome.model';
import { Api } from '../../services/api';
import { CommonModule } from '@angular/common';
import { LanguageService } from '../../services/language.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-postgrad-induction',
  imports: [RouterModule, CommonModule, TranslateModule],
  templateUrl: './postgrad-induction.html',
  styleUrl: './postgrad-induction.css',
})
export class PostgradInduction {
  pageData: LevelData | null = null;
  isLoading = true;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private welcomeService: Api,
    private cdr: ChangeDetectorRef,
    private languageService: LanguageService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const level = params.get('level');
      if (level) {
        this.fetchInductionData(level, this.languageService.getCurrentLanguage());
      }
    });
  }

  fetchInductionData(level: string, languageCode: string): void {
    this.isLoading = true;
    this.welcomeService.getOnboardingConfigByLevel(level, languageCode).subscribe({
      next: (data) => {
        this.pageData = data;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading welcome data:', err);
        this.errorMessage = 'Could not load the welcome guide. Please try again later.';
        this.isLoading = false;
      }
    });
  }

  getBadgeClass(type: string): string {
    switch (type) {
      case 'action': return 'bg-primary';
      case 'deadline': return 'bg-danger';
      case 'event': return 'bg-success';
      default: return 'bg-secondary';
    }
  }
}

import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { LevelData } from '../../models/welcome.model';
import { ApiService } from '../../services/api.service';
import { LoggerService } from '../../services/logger.service';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-postgrad-induction',
  imports: [RouterModule, CommonModule, TranslateModule],
  templateUrl: './postgrad-induction.html',
  styleUrl: './postgrad-induction.css',
})
export class PostgradInduction implements OnInit {
  private route = inject(ActivatedRoute);
  private apiService = inject(ApiService);
  private logger = inject(LoggerService);
  private destroyRef = inject(DestroyRef);

  pageData = signal<LevelData | null>(null);
  isLoading = signal(true);
  errorMessage = signal('');

  ngOnInit(): void {
    this.route.paramMap
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(params => {
        const level = params.get('level');
        if (level) {
          this.fetchInductionData(level);
        }
      });
  }

  fetchInductionData(level: string): void {
    this.isLoading.set(true);
    this.apiService.getOnboardingConfigByLevel(level).subscribe({
      next: (data) => {
        this.pageData.set(data);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.logger.error('Error loading welcome data:', err);
        this.errorMessage.set('Could not load the welcome guide. Please try again later.');
        this.isLoading.set(false);
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

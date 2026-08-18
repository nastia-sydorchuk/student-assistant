import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ApiService } from '../../services/api.service';
import { RouterLink, RouterModule } from '@angular/router';
import { PortalCard } from '../../models/portal-card.model';
import { LanguageService } from '../../services/language.service';
import { LoggerService } from '../../services/logger.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-landing',
  imports: [CommonModule, RouterModule, RouterLink, TranslateModule],
  templateUrl: './landing.html',
  styleUrl: './landing.css',
})
export class Landing implements OnInit {
  private apiService = inject(ApiService);
  private languageService = inject(LanguageService);
  private logger = inject(LoggerService);
  private destroyRef = inject(DestroyRef);

  portalCards = signal<PortalCard[]>([]);
  isLoading = signal(true);

  ngOnInit(): void {
    this.languageService.currentLanguage$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.apiService.getPortalCards().subscribe({
          next: (data) => {
            this.portalCards.set(data);
            this.isLoading.set(false);
          },
          error: (err) => {
            this.logger.error('Failed to load dashboard config', err);
            this.isLoading.set(false);
          }
        });
      });
  }
}

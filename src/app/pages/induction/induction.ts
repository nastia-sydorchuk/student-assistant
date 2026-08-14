import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ApiService } from '../../services/api.service';
import { InductionEvent } from '../../models/welcome.model';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { LanguageService } from '../../services/language.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-induction',
  imports: [RouterLink, CommonModule, TranslateModule],
  templateUrl: './induction.html',
  styleUrl: './induction.css',
})
export class Induction {
  private apiService = inject(ApiService);
  private languageService = inject(LanguageService);
  private destroyRef = inject(DestroyRef);

  inductionEvents = signal<InductionEvent[]>([]);
  isLoadingEvents = signal(true);
  eventsError = signal('');

  ngOnInit() {
    this.languageService.currentLanguage$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.fetchInductionEvents();
      });
  }

  fetchInductionEvents(): void {
    this.isLoadingEvents.set(true);

    this.apiService.getInductionEvents().subscribe({
      next: (data) => {
        this.inductionEvents.set(data);
        this.isLoadingEvents.set(false);
      },
      error: (error) => {
        console.error('Error fetching events:', error);
        this.eventsError.set('Unable to load induction events at this time. Please try again later.');
        this.isLoadingEvents.set(false);
      }
    });
  }
}

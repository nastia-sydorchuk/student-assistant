import { DestroyRef, Directive, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ResourceCategory } from '../models/resources.model';
import { ApiService } from '../services/api.service';
import { LanguageService } from '../services/language.service';

@Directive()
export abstract class ResourceCategoryPage implements OnInit {
  protected abstract categoryId: string;

  private apiService = inject(ApiService);
  private languageService = inject(LanguageService);
  private destroyRef = inject(DestroyRef);

  resources = signal<ResourceCategory | null>(null);
  isLoading = signal(true);
  errorMessage = signal('');

  ngOnInit(): void {
    this.languageService.currentLanguage$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.loadCategory());
  }

  private loadCategory(): void {
    this.isLoading.set(true);

    this.apiService.getResources(this.categoryId).subscribe({
      next: (data) => {
        this.resources.set(data);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Could not load resources at this time. Please try again later.');
        this.isLoading.set(false);
      }
    });
  }
}

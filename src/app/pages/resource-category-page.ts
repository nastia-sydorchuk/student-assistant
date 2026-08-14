import { DestroyRef, Directive, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ResourceCategory } from '../models/resources.model';
import { Api } from '../services/api';
import { LanguageService } from '../services/language.service';

@Directive()
export abstract class ResourceCategoryPage {
  protected abstract categoryId: string;

  private resourceService = inject(Api);
  private languageService = inject(LanguageService);
  private destroyRef = inject(DestroyRef);

  resources = signal<ResourceCategory | null>(null);
  isLoading = signal(true);
  errorMessage = signal('');

  ngOnInit(): void {
    this.languageService.currentLanguage$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(newLang => this.loadCategory(newLang));
  }

  private loadCategory(languageCode: string): void {
    this.isLoading.set(true);

    this.resourceService.getResources(this.categoryId, languageCode).subscribe({
      next: (data) => {
        this.resources.set(data);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error fetching resources:', error);
        this.errorMessage.set('Could not load resources at this time. Please try again later.');
        this.isLoading.set(false);
      }
    });
  }
}

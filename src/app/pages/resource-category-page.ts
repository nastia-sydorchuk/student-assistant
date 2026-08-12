import { ChangeDetectorRef, DestroyRef, Directive, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ResourceCategory } from '../models/resources.model';
import { Api } from '../services/api';
import { LanguageService } from '../services/language.service';

@Directive()
export abstract class ResourceCategoryPage {
  protected abstract categoryId: string;

  private resourceService = inject(Api);
  private cdr = inject(ChangeDetectorRef);
  private languageService = inject(LanguageService);
  private destroyRef = inject(DestroyRef);

  resources: ResourceCategory | null = null;
  isLoading = true;
  errorMessage = '';

  ngOnInit(): void {
    this.languageService.currentLanguage$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(newLang => this.loadCategory(newLang));
  }

  private loadCategory(languageCode: string): void {
    this.isLoading = true;

    this.resourceService.getResources(this.categoryId, languageCode).subscribe({
      next: (data) => {
        this.resources = data;
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
}

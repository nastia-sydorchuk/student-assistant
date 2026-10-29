import { Directive, OnInit, inject } from '@angular/core';
import { ResourceCategory } from '../models/resources.model';
import { ApiService } from '../services/api.service';
import { LanguageAwareLoader } from './language-aware-loader';

@Directive()
export abstract class ResourceCategoryPage implements OnInit {
  protected abstract categoryId: string;

  private apiService = inject(ApiService);
  private category = new LanguageAwareLoader<ResourceCategory | null>(null, 'COMMON.ERROR');

  resources = this.category.data;
  isLoading = this.category.isLoading;
  errorMessage = this.category.error;

  ngOnInit(): void {
    this.category.load(() => this.apiService.getResources(this.categoryId));
  }
}

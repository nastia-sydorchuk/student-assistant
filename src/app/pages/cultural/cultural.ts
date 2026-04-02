import { ChangeDetectorRef, Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ResourceCategory } from '../../models/resources.model';
import { Api } from '../../services/api';
import { CommonModule } from '@angular/common';
import { LanguageService } from '../../services/language.service';

@Component({
  selector: 'app-cultural',
  imports: [RouterLink, CommonModule],
  templateUrl: './cultural.html',
  styleUrl: './cultural.css',
})
export class Cultural {
  culturalResources: ResourceCategory | null = null;
  
  isLoading = true;
  errorMessage = '';

  constructor(private resourceService: Api, private cdr: ChangeDetectorRef, private languageService: LanguageService) {}

  ngOnInit(): void {
    this.languageService.currentLanguage$.subscribe(newLang => {
      this.loadCategory('cultural', newLang);
    });
  }

  loadCategory(categoryId: string, languageCode: string): void {
    this.isLoading = true;
    
    this.resourceService.getResources(categoryId, languageCode).subscribe({
      next: (data) => {
        this.culturalResources = data;
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

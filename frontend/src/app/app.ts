import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { ChatWidget } from './components/chat-widget/chat-widget';
import { LanguageService } from './services/language.service';
import { ApiService } from './services/api.service';
import { Language } from './models/language.model';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ChatWidget, RouterLink, TranslateModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  private languageService = inject(LanguageService);
  private apiService = inject(ApiService);

  protected readonly title = signal('frontend');
  currentLang = this.languageService.getCurrentLanguage();
  languages = signal<Language[]>([]);

  ngOnInit(): void {
    this.apiService.getLanguages().subscribe({
      next: (data) => this.languages.set(data),
      error: () => this.languages.set([])
    });
  }

  onLanguageChange(event: Event): void {
    const selectElement = event.target as HTMLSelectElement;
    this.currentLang = selectElement.value;
    this.languageService.changeLanguage(selectElement.value);
  }
}

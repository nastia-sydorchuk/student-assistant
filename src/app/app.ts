import { Component, signal } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { ChatWidget } from './components/chat-widget/chat-widget';
import { LanguageService } from './services/language.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ChatWidget, RouterLink],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('frontend');
  currentLang = 'EN';

  constructor(private languageService: LanguageService) {
    this.currentLang = this.languageService.getCurrentLanguage();
  }

  onLanguageChange(event: Event): void {
    const selectElement = event.target as HTMLSelectElement;
    this.languageService.changeLanguage(selectElement.value);
  }
}

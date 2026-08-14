import { Component, inject, signal } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { ChatWidget } from './components/chat-widget/chat-widget';
import { LanguageService } from './services/language.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ChatWidget, RouterLink, TranslateModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  private languageService = inject(LanguageService);

  protected readonly title = signal('frontend');
  currentLang = this.languageService.getCurrentLanguage();

  onLanguageChange(event: Event): void {
    const selectElement = event.target as HTMLSelectElement;
    this.languageService.changeLanguage(selectElement.value);
  }
}

import { Component, signal } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { ChatWidget } from './components/chat-widget/chat-widget';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ChatWidget, RouterLink],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('frontend');
  currentLanguage = 'EN';

  toggleTranslation() {
    this.currentLanguage = this.currentLanguage === 'EN' ? 'ES' : 'EN';
    console.log(`Language switched to: ${this.currentLanguage}`);
    // hook into Angular i18n or ngx-translate
  }
}

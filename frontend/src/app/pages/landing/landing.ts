import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { RouterLink, RouterModule } from '@angular/router';
import { PortalCard } from '../../models/portal-card.model';
import { TranslateModule } from '@ngx-translate/core';
import { LanguageAwareLoader } from '../shared/language-aware-loader';

@Component({
  selector: 'app-landing',
  imports: [CommonModule, RouterModule, RouterLink, TranslateModule],
  templateUrl: './landing.html',
  styleUrl: './landing.css',
})
export class Landing implements OnInit {
  private apiService = inject(ApiService);

  private cards = new LanguageAwareLoader<PortalCard[]>([], 'DASHBOARD.ERROR');

  portalCards = this.cards.data;
  isLoading = this.cards.isLoading;
  errorMessage = this.cards.error;

  ngOnInit(): void {
    this.cards.load(() => this.apiService.getPortalCards());
  }
}

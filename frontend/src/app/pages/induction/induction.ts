import { Component, OnInit, inject } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { InductionEvent } from '../../models/welcome.model';
import { PageHeader } from '../../components/page-header/page-header';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { SmartLinkDirective } from '../../directives/smart-link.directive';
import { LanguageAwareLoader } from '../shared/language-aware-loader';

@Component({
  selector: 'app-induction',
  imports: [PageHeader, CommonModule, TranslateModule, SmartLinkDirective],
  templateUrl: './induction.html',
  styleUrl: './induction.css',
})
export class Induction implements OnInit {
  private apiService = inject(ApiService);

  private events = new LanguageAwareLoader<InductionEvent[]>([], 'INDUCTION.EVENTS_ERROR');

  inductionEvents = this.events.data;
  isLoadingEvents = this.events.isLoading;
  eventsError = this.events.error;

  ngOnInit(): void {
    this.events.load(() => this.apiService.getInductionEvents());
  }
}

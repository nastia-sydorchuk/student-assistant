import { Component, OnInit, inject } from '@angular/core';
import { Step } from '../../models/welcome.model';
import { ApiService } from '../../services/api.service';
import { PageHeader } from '../../components/page-header/page-header';
import { LoadState } from '../../components/load-state/load-state';
import { CommonModule } from '@angular/common';
import { ResourceCategory } from '../../models/resources.model';
import { TranslateModule } from '@ngx-translate/core';
import { SmartLinkDirective } from '../../directives/smart-link.directive';
import { LanguageAwareLoader, combinedState } from '../language-aware-loader';

@Component({
  selector: 'app-registration',
  imports: [PageHeader, LoadState, CommonModule, TranslateModule, SmartLinkDirective],
  templateUrl: './registration.html',
  styleUrl: './registration.css',
})
export class Registration implements OnInit {
  private apiService = inject(ApiService);

  private steps = new LanguageAwareLoader<Step[] | null>(null, 'REGISTRATION.STEPS_ERROR');
  private tech = new LanguageAwareLoader<ResourceCategory | null>(null, 'COMMON.ERROR');
  private state = combinedState(this.steps, this.tech);

  onboardingSteps = this.steps.data;
  techResources = this.tech.data;
  isLoading = this.state.isLoading;
  errorMessage = this.state.error;

  ngOnInit(): void {
    this.tech.load(() => this.apiService.getResources('tech'));
    this.steps.load(() => this.apiService.getOnboardingSteps());
  }
}

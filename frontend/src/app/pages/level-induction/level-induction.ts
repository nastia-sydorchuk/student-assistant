import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { filter, map } from 'rxjs';
import { LevelData } from '../../models/welcome.model';
import { ApiService } from '../../services/api.service';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { LanguageAwareLoader } from '../language-aware-loader';

@Component({
  selector: 'app-level-induction',
  imports: [RouterModule, CommonModule, TranslateModule],
  templateUrl: './level-induction.html',
  styleUrl: './level-induction.css',
})
export class LevelInduction implements OnInit {
  private route = inject(ActivatedRoute);
  private apiService = inject(ApiService);

  private level = new LanguageAwareLoader<LevelData | null>(null, 'INDUCTION.ERROR');

  pageData = this.level.data;
  isLoading = this.level.isLoading;
  errorMessage = this.level.error;

  ngOnInit(): void {
    // Reloads when either the level in the URL or the language changes
    const level$ = this.route.paramMap.pipe(
      map((params) => params.get('level')),
      filter((level): level is string => !!level),
    );
    this.level.load((level) => this.apiService.getOnboardingConfigByLevel(level), level$);
  }

  getBadgeClass(type: string): string {
    switch (type) {
      case 'action': return 'bg-primary';
      case 'deadline': return 'bg-danger';
      case 'event': return 'bg-success';
      default: return 'bg-secondary';
    }
  }
}

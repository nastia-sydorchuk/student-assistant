import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { Api } from '../../services/api';
import { RouterLink, RouterModule } from '@angular/router';
import { PortalCard } from '../../models/portal-card.model';
import { LanguageService } from '../../services/language.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-landing',
  imports: [CommonModule, RouterModule, RouterLink, TranslateModule],
  templateUrl: './landing.html',
  styleUrl: './landing.css',
})
export class Landing {
  portalCards: PortalCard[] = [];
  isLoading = true;

  constructor(private uiService: Api, private cdr: ChangeDetectorRef, private languageService: LanguageService) {}

  ngOnInit(): void {
    this.languageService.currentLanguage$.subscribe((languageCode) => {
      this.uiService.getPortalCards(languageCode).subscribe({
        next: (data) => {
          this.portalCards = data;
          this.isLoading = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Failed to load dashboard config', err);
          this.isLoading = false;
        }
      });
    });
  }
  // portalCards = [
  //   { 
  //     title: 'Registration & Starting your studies', 
  //     route: '/registration', 
  //     icon: 'how_to_reg', 
  //     description: 'Complete your university enrollment, select your modules, and set up your essential digital tools.' 
  //   },
  //   { 
  //     title: 'University services', 
  //     route: '/services', 
  //     icon: 'domain', 
  //     description: 'Student support, administration, and essential university services.' 
  //   },
  //   { 
  //     title: 'Academic resources', 
  //     route: '/academic', 
  //     icon: 'local_library', 
  //     description: 'Access libraries, research databases, and tutoring centers.' 
  //   },
  //   { 
  //     title: 'Campus facilities', 
  //     route: '/facilities', 
  //     icon: 'map', 
  //     description: 'Explore gyms, health centre, study spaces, dining halls, and campus maps.' 
  //   },
  //   { 
  //     title: 'Cultural integration', 
  //     route: '/cultural', 
  //     icon: 'public', 
  //     description: 'Join clubs, attend events, and build your community.' 
  //   }
  // ];
}

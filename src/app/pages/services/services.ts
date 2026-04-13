import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ResourceCategory } from '../../models/resources.model';
import { Api } from '../../services/api';
import { LanguageService } from '../../services/language.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-services',
  imports: [RouterLink, CommonModule, TranslateModule],
  templateUrl: './services.html',
  styleUrl: './services.css',
})
export class Services {
  universityServices: ResourceCategory | null = null;
  
  isLoading = true;
  errorMessage = '';

  constructor(private resourceService: Api, private cdr: ChangeDetectorRef, private languageService: LanguageService) {}

  ngOnInit(): void {
    this.languageService.currentLanguage$.subscribe(newLang => {
      this.loadCategory('services', newLang);
    });
  }

  loadCategory(categoryId: string, languageCode: string): void {
    this.isLoading = true;
    
    this.resourceService.getResources(categoryId, languageCode).subscribe({
      next: (data) => {
        this.universityServices = data;
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

  // universityServices = [
  //   {
  //     title: 'AskHWU Student Support',
  //     icon: 'support_agent',
  //     description: 'The primary contact for student ID cards, enrollment letters, council tax exemptions, and general inquiries.',
  //     contact: 'askhwu@hw.ac.uk',
  //     action: 'Visit AskHWU Portal',
  //     url: 'https://myhwu.hw.ac.uk/'
  //   },
  //   {
  //     title: 'Student Wellbeing',
  //     icon: 'health_and_safety',
  //     description: 'Access counseling, disability support, and mental health resources to help you thrive during your studies.',
  //     contact: 'studentwellbeing@hw.ac.uk',
  //     action: 'View Wellbeing Services',
  //     url: '/wellbeing'
  //   },
  //   {
  //     title: 'Information Services (IT Help)',
  //     icon: 'computer',
  //     description: 'Support for eduroam Wi-Fi, Office 365, password resets, and accessing university software.',
  //     contact: 'ithelp@hw.ac.uk',
  //     action: 'Access IT Support',
  //     url: 'https://www.hw.ac.uk/about/professional-services/information-services/is-guides/getting-started'
  //   },
  //   {
  //     title: 'Careers & Graduate Futures',
  //     icon: 'work',
  //     description: 'Get CV advice, practice interviews, and discover graduate roles and part-time jobs through the Careers portal.',
  //     contact: 'careers@hw.ac.uk',
  //     action: 'Browse Careers Portal',
  //     url: 'https://www.hw.ac.uk/uk/students/careers.htm'
  //   },
  //   {
  //     title: 'Accommodation Services',
  //     icon: 'apartment',
  //     description: 'Manage your on-campus housing, report maintenance issues, and find off-campus renting advice.',
  //     contact: 'reslife@hw.ac.uk',
  //     action: 'Manage Housing',
  //     url: 'https://www.hw.ac.uk/uk/edinburgh/accommodation.htm'
  //   },
  //   {
  //     title: 'Student Funding & Fees',
  //     icon: 'account_balance',
  //     description: 'Information regarding tuition fees, scholarships, bursaries, and hardship funds.',
  //     contact: 'studentfees@hw.ac.uk',
  //     action: 'View Financial Support',
  //     url: 'https://www.hw.ac.uk/study/fees-and-funding/tuition-fees'
  //   }
  // ];
}

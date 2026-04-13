import { ChangeDetectorRef, Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ResourceCategory } from '../../models/resources.model';
import { Api } from '../../services/api';
import { CommonModule } from '@angular/common';
import { LanguageService } from '../../services/language.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-wellbeing',
  imports: [RouterLink, CommonModule, TranslateModule],
  templateUrl: './wellbeing.html',
  styleUrl: './wellbeing.css',
})
export class Wellbeing {
  wellbeingResources: ResourceCategory | null = null;
  
  isLoading = true;
  errorMessage = '';

  constructor(private resourceService: Api, private cdr: ChangeDetectorRef, private languageService: LanguageService) {}

  ngOnInit(): void {
    this.languageService.currentLanguage$.subscribe(newLang => {
      this.loadCategory('wellbeing', newLang);
    });
  }

  loadCategory(categoryId: string, languageCode: string): void {
    this.isLoading = true;
    
    this.resourceService.getResources(categoryId, languageCode).subscribe({
      next: (data) => {
        this.wellbeingResources = data;
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

  // wellbeingResources = [
  //   {
  //     title: 'Register with a Doctor (GP)',
  //     icon: 'local_hospital',
  //     description: 'Register with the on-campus Medical Centre (Richard Verden Anderson building) or a local NHS GP to ensure you can access healthcare and prescriptions.',
  //     actionText: 'Register with Medical Centre',
  //     url: 'https://www.riccartongeneralpractice.co.uk/student'
  //   },
  //   {
  //     title: 'Student Wellbeing Centre',
  //     icon: 'psychology_alt',
  //     description: 'Professional, confidential support for mental health, emotional difficulties, and personal challenges. Includes 1-to-1 counseling.',
  //     actionText: 'Book an Appointment',
  //     url: 'https://www.hw.ac.uk/uk/students/health-wellbeing/edinburgh/wellbeing-services.htm'
  //   },
  //   {
  //     title: 'Disability Service',
  //     icon: 'accessible',
  //     description: 'Advice and support for students with physical disabilities, specific learning difficulties (like Dyslexia), and long-term health conditions.',
  //     actionText: 'Explore Support',
  //     url: 'https://www.hw.ac.uk/uk/students/health-wellbeing/edinburgh/disability.htm'
  //   },
  //   {
  //     title: 'Togetherall (24/7 Support)',
  //     icon: 'forum',
  //     description: 'A safe, online, and anonymous community to support your mental health, available 24/7 and free for all Heriot-Watt students.',
  //     actionText: 'Join Togetherall',
  //     url: 'https://togetherall.com/' 
  //   },
  //   {
  //     title: 'Report and Support',
  //     icon: 'campaign',
  //     description: 'Report incidents of bullying, harassment, sexual violence, or discrimination securely. You can report anonymously or request contact.',
  //     actionText: 'Make a Report',
  //     url: 'https://reportandsupport.hw.ac.uk/'
  //   },
  //   {
  //     title: 'SafeZone App',
  //     icon: 'security',
  //     description: 'A free app connecting you directly to the university security team for immediate help, first aid, or emergency assistance on campus.',
  //     actionText: 'Download SafeZone',
  //     url: 'http://hw.ac.uk/about/professional-services/governance-and-legal-services/safeguarding-services/safezone'
  //   },
  //   {
  //     title: 'Chaplaincy',
  //     icon: 'spa',
  //     description: 'A welcoming space for people of all faiths and none. Drop in for a coffee, quiet reflection, mindfulness sessions, or pastoral care.',
  //     actionText: 'Visit the Chaplaincy',
  //     url: 'https://www.hw.ac.uk/students/health-and-wellbeing/edinburgh-campus/faith-and-belief/chaplaincy'
  //   }
  // ];
}

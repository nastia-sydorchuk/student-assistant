import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Map } from '../../components/map/map';
import { CommonModule, ViewportScroller } from '@angular/common';
import { ResourceCategory } from '../../models/resources.model';
import { Api } from '../../services/api';
import { LanguageService } from '../../services/language.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-facilities',
  imports: [RouterLink, Map, CommonModule, TranslateModule],
  templateUrl: './facilities.html',
  styleUrl: './facilities.css',
})
export class Facilities {
  private scroller = inject(ViewportScroller);

  activeMapLocation: string | null = null;
  
  facilities: ResourceCategory | null = null;
  
  isLoading = true;
  errorMessage = '';

  constructor(private resourceService: Api, private cdr: ChangeDetectorRef, private languageService: LanguageService) {}

  ngOnInit(): void {
    this.languageService.currentLanguage$.subscribe(newLang => {
      this.loadCategory('facilities', newLang);
    });
  }

  loadCategory(categoryId: string, languageCode: string): void {
    this.isLoading = true;
    
    this.resourceService.getResources(categoryId, languageCode).subscribe({
      next: (data) => {
        this.facilities = data;
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

  // facilities = [
  //   { 
  //     name: 'Oriam, Scotland’s Sports Performance Centre', 
  //     icon: 'fitness_center', 
  //     desc: 'World-class gym, sports halls, squash courts, and fitness classes for students and elite athletes.', 
  //     location: 'Oriam Building',
  //     actionText: 'Explore Oriam',
  //     url: 'https://www.oriamscotland.com/'
  //   },
  //   { 
  //     name: 'Hugh Nisbet Building', 
  //     icon: 'restaurant', 
  //     desc: 'The central hub featuring the main dining room, shops, the Student Union, and Geordies bar.', 
  //     location: 'Hugh Nisbet Building',
  //     actionText: 'View Food & Drink',
  //     url: 'https://www.hw.ac.uk/campuses/edinburgh/facilities/catering'
  //   },
  //   { 
  //     name: 'The GRID', 
  //     icon: 'lightbulb', 
  //     desc: 'Global Research Innovation and Discovery centre. High-tech study spaces, VR labs, and enterprise hubs.', 
  //     location: 'GRID Building',
  //     actionText: 'Discover The GRID',
  //     url: 'https://www.hw.ac.uk/uk/edinburgh/grid.htm'
  //   },
  //   { 
  //     name: 'Student Health Centre', 
  //     icon: 'medical_services', 
  //     desc: 'Independent NHS general practice and dental surgery located right on the university campus.', 
  //     location: 'Riccarton General Practice',
  //     actionText: 'Register with GP',
  //     url: 'https://www.riccartongeneralpractice.co.uk/student'
  //   },
  //   {
  //     name: 'Library',
  //     icon: 'local_library',
  //     desc: 'Access print and electronic holdings, or book digital study studios.',
  //     location: 'Library Building',
  //     actionText: 'Discover Library Resources',
  //     url: 'https://www.hw.ac.uk/about/professional-services/information-services/library-essentials'
  //   },
  //   { 
  //     name: 'Student Union', 
  //     icon: 'groups', 
  //     desc: 'A central hub for clubs, societies, and student support.', 
  //     location: 'Heriot-Watt University Student Union, Edinburgh',
  //     actionText: 'Visit the Union',
  //     url: 'https://www.hwunion.com/'
  //   },
  // ];

  highlightOnMap(location: string, event: Event) {
    event.preventDefault();
    this.activeMapLocation = location;
    this.scroller.scrollToAnchor('campus-map'); 
  }
}

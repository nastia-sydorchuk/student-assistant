import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Map } from '../../components/map/map';
import { ViewportScroller } from '@angular/common';

@Component({
  selector: 'app-facilities',
  imports: [RouterLink, Map],
  templateUrl: './facilities.html',
  styleUrl: './facilities.css',
})
export class Facilities {
  private scroller = inject(ViewportScroller);

  activeMapLocation: string | null = null;
  
  facilities = [
    { 
      name: 'Oriam, Scotland’s Sports Performance Centre', 
      icon: 'fitness_center', 
      desc: 'World-class gym, sports halls, squash courts, and fitness classes for students and elite athletes.', 
      location: 'Oriam Building',
      actionText: 'Explore Oriam',
      url: 'https://www.oriamscotland.com/'
    },
    { 
      name: 'Hugh Nisbet Building', 
      icon: 'restaurant', 
      desc: 'The central hub featuring the main dining room, shops, the Student Union, and Geordies bar.', 
      location: 'Hugh Nisbet Building',
      actionText: 'View Food & Drink',
      url: 'https://www.hw.ac.uk/uk/edinburgh/food-drink.htm'
    },
    { 
      name: 'The GRID', 
      icon: 'lightbulb', 
      desc: 'Global Research Innovation and Discovery centre. High-tech study spaces, VR labs, and enterprise hubs.', 
      location: 'GRID Building',
      actionText: 'Discover The GRID',
      url: 'https://www.hw.ac.uk/uk/edinburgh/grid.htm'
    },
    { 
      name: 'Student Health Centre', 
      icon: 'medical_services', 
      desc: 'Independent NHS general practice and dental surgery located right on the university campus.', 
      location: 'Riccarton General Practice',
      actionText: 'Register with GP',
      url: 'https://www.riccartongeneralpractice.co.uk/student'
    },
    {
      name: 'Library',
      icon: 'local_library',
      desc: 'Access print and electronic holdings, or book digital study studios.',
      location: 'Library Building',
      actionText: 'See How to Borrow',
      url: 'http://hw.ac.uk/about/professional-services/information-services/library-essentials/borrowing-from-hwu-libraries'
    },
  ];
    // { 
    //   name: 'Science & Engineering Labs', 
    //   icon: 'science', 
    //   desc: '24/7 access for enrolled students with proper safety clearance.', 
    //   location: 'Tech Building' 
    // },
    // { 
    //   name: 'Student Union', 
    //   icon: 'groups', 
    //   desc: 'Relaxation lounges, event spaces, and student organization offices.', 
    //   location: 'South Campus' 
    // },

  highlightOnMap(location: string, event: Event) {
    event.preventDefault();
    this.activeMapLocation = location;
    this.scroller.scrollToAnchor('campus-map'); 
  }
}

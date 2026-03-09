import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Api } from '../../services/api';
import { RouterLink, RouterModule } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-landing',
  imports: [CommonModule, RouterModule, RouterLink],
  templateUrl: './landing.html',
  styleUrl: './landing.css',
})
export class Landing {
  portalCards = [
    { 
      title: 'Registration & Starting your studies', 
      route: '/registration', 
      icon: 'how_to_reg', 
      description: 'Complete your university enrollment, select your modules, and set up your essential digital tools.' 
    },
    { 
      title: 'University services', 
      route: '/services', 
      icon: 'domain', 
      description: 'Student support, administration, and essential university services.' 
    },
    { 
      title: 'Academic resources', 
      route: '/academic', 
      icon: 'local_library', 
      description: 'Access libraries, research databases, and tutoring centers.' 
    },
    { 
      title: 'Campus facilities', 
      route: '/facilities', 
      icon: 'map', 
      description: 'Explore gyms, health centre, study spaces, dining halls, and campus maps.' 
    },
    { 
      title: 'Cultural integration', 
      route: '/cultural', 
      icon: 'public', 
      description: 'Join clubs, attend events, and build your community.' 
    }
  ];

  constructor(private service: Api, public dialog: MatDialog) {}

  ngOnInit() {
    //this.send()
  }
}

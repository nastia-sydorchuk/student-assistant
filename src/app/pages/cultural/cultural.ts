import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-cultural',
  imports: [RouterLink],
  templateUrl: './cultural.html',
  styleUrl: './cultural.css',
})
export class Cultural {
  culturalResources = [
    {
      title: 'Heriot-Watt Student Union',
      icon: 'diversity_3',
      description: 'The heart of student life. Join over 80 societies, sports clubs, and get involved in student elections.',
      actionText: 'Visit HWUnion',
      url: 'https://www.hwunion.com/'
    },
    {
      title: 'Global Student Office',
      icon: 'public',
      description: 'Visa support, immigration advice, and resources designed specifically to help international students settle in.',
      actionText: 'View Global Support',
      url: 'https://www.hw.ac.uk/uk/students/international.htm'
    },
    {
      title: 'Chaplaincy & Faith',
      icon: 'spa',
      description: 'A welcoming space for people of all faiths and none. Drop in for a coffee, quiet reflection, or multi-faith worship.',
      actionText: 'Discover Chaplaincy',
      url: 'https://www.hw.ac.uk/uk/students/health-wellbeing/edinburgh/chaplaincy.htm'
    },
    {
      title: 'University Events',
      icon: 'event',
      description: 'Keep up to date with guest lectures, career fairs, cultural festivals, and university-wide celebrations.',
      actionText: 'View Calendar',
      url: 'http://hw.ac.libcal.com/calendar/studentevents'
    },
    {
      title: 'Volunteer Action',
      icon: 'volunteer_activism',
      description: 'Make a difference in the local Edinburgh community and boost your employability by volunteering through the Union.',
      actionText: 'Find Opportunities',
      url: 'https://www.hwunion.com/volunteering/'
    },
    {
      title: 'Sports Union (Oriam)',
      icon: 'sports_soccer',
      description: 'Join a sports team, take fitness classes, and access Scotland’s Sports Performance Centre right on campus.',
      actionText: 'Explore Oriam',
      url: 'https://www.oriamscotland.com/'
    }
  ];
    // {
    //   title: 'Diversity & Inclusion',
    //   icon: 'handshake',
    //   description: 'Resources, advocacy groups, and training to foster a safe and inclusive campus for everyone.',
    //   actionText: 'Learn More'
    // }
}

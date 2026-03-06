import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-wellbeing',
  imports: [RouterLink],
  templateUrl: './wellbeing.html',
  styleUrl: './wellbeing.css',
})
export class Wellbeing {
  wellbeingResources = [
    {
      title: 'Register with a Doctor (GP)',
      icon: 'local_hospital',
      description: 'Register with the on-campus Medical Centre (Richard Verden Anderson building) or a local NHS GP to ensure you can access healthcare and prescriptions.',
      actionText: 'Register with Medical Centre',
      url: 'https://www.hw.ac.uk/uk/students/health-wellbeing/edinburgh/medical-centre.htm'
    },
    {
      title: 'Student Wellbeing Centre',
      icon: 'psychology_alt',
      description: 'Professional, confidential support for mental health, emotional difficulties, and personal challenges. Includes 1-to-1 counseling.',
      actionText: 'Book an Appointment',
      url: 'https://www.hw.ac.uk/uk/students/health-wellbeing/edinburgh/wellbeing-services.htm'
    },
    {
      title: 'Disability Service',
      icon: 'accessible',
      description: 'Advice and support for students with physical disabilities, specific learning difficulties (like Dyslexia), and long-term health conditions.',
      actionText: 'Explore Support',
      url: 'https://www.hw.ac.uk/uk/students/health-wellbeing/edinburgh/disability.htm'
    },
    {
      title: 'Togetherall (24/7 Support)',
      icon: 'forum',
      description: 'A safe, online, and anonymous community to support your mental health, available 24/7 and free for all Heriot-Watt students.',
      actionText: 'Join Togetherall',
      url: 'https://togetherall.com/' 
    },
    {
      title: 'Report and Support',
      icon: 'campaign',
      description: 'Report incidents of bullying, harassment, sexual violence, or discrimination securely. You can report anonymously or request contact.',
      actionText: 'Make a Report',
      url: 'https://reportandsupport.hw.ac.uk/'
    },
    {
      title: 'SafeZone App',
      icon: 'security',
      description: 'A free app connecting you directly to the university security team for immediate help, first aid, or emergency assistance on campus.',
      actionText: 'Download SafeZone',
      url: 'https://www.hw.ac.uk/uk/edinburgh/safezone.htm'
    },
    {
      title: 'Chaplaincy & Mindfulness',
      icon: 'spa',
      description: 'A welcoming space for people of all faiths and none. Drop in for a coffee, quiet reflection, mindfulness sessions, or pastoral care.',
      actionText: 'Visit the Chaplaincy',
      url: 'https://www.hw.ac.uk/uk/students/health-wellbeing/edinburgh/chaplaincy.htm'
    }
  ];
}

import { ChangeDetectorRef, Component } from '@angular/core';
import { Step, WelcomePage } from '../../models/welcome.model';
import { Api } from '../../services/api';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-registration',
  imports: [RouterLink, CommonModule],
  templateUrl: './registration.html',
  styleUrl: './registration.css',
})
export class Registration {
  onboardingSteps = [
    {
      step: 1,
      title: 'Online Enrolment',
      description: 'Log in to Student Self Service to confirm your personal details, programme of study, and complete official enrolment.',
      actionText: 'Go to Student Self Service',
      url: 'https://myhwu.hw.ac.uk/',
      isSecondary: false
    },
    {
      step: 2,
      title: 'Courses Selection',
      description: 'Review your mandatory courses and select your optional courses for the upcoming academic year.',
      actionText: 'View Course Selection',
      url: 'https://curriculum.hw.ac.uk/',
      isSecondary: false
    },
    {
      step: 3,
      title: 'Registering for classes',
      description: 'Register for your selected courses to get access to the materials and resources on Canvas.',
      actionText: 'Register for Classes',
      url: 'https://curriculum.hw.ac.uk/',
      isSecondary: false
    },
    // {
    //   step: 3,
    //   title: 'Tuition Fees & Payments',
    //   description: 'Review your fee status, apply for SAAS/student finance, or set up a payment plan with the university.',
    //   actionText: 'Manage Payments',
    //   url: 'https://www.hw.ac.uk/uk/students/money/pay.htm',
    //   isSecondary: false
    // },
    {
      step: 4,
      title: 'Welcome Week Schedule',
      description: 'Check the official Welcome Guide for orientation schedules, and Freshers activities.',
      actionText: 'View Welcome Guide',
      url: '/induction',
      isSecondary: false
    }
  ];

  // Section 2: IT and Digital Setup
  techResources = [
    {
      title: 'Connect to eduroam Wi-Fi',
      icon: 'wifi',
      description: 'Set up the secure eduroam network on your phone and laptop for free, seamless internet access across all campuses.',
      actionText: 'Wi-Fi Setup Guide',
      url: 'https://www.hw.ac.uk/uk/services/is/it-essentials/wifi.htm',
      label: 'Connectivity',
      badgeClass: 'text-bg-success'
    },
    {
      title: 'Multi-Factor Authentication (MFA)',
      icon: 'phonelink_lock',
      description: 'Secure your university account. Download the Microsoft Authenticator app to set up mandatory two-step verification.',
      actionText: 'Set up MFA',
      url: 'https://www.hw.ac.uk/uk/services/is/it-essentials/mfa.htm',
      label: 'Security',
      badgeClass: 'text-bg-danger'
    },
    {
      title: 'Student Email (Office 365)',
      icon: 'mail',
      description: 'Access your official Heriot-Watt email. All university communications, timetable updates, and announcements are sent here.',
      actionText: 'Access Webmail',
      url: 'https://outlook.office.com/mail/',
      label: 'Communication',
      badgeClass: 'text-bg-primary'
    },
    {
      title: 'Free Software & AppsAnywhere',
      icon: 'apps',
      description: 'Download Microsoft Office 365 for free, or use AppsAnywhere to stream course-specific software directly to your own device.',
      actionText: 'Get Software',
      url: 'https://www.hw.ac.uk/uk/services/is/it-essentials/software.htm',
      label: 'Software',
      badgeClass: 'text-bg-info'
    }
  ];

  // onboardingSteps: Step[] = [];
  // isLoading = true;
  // eventsError = '';

  // constructor(private service: Api, private cdr: ChangeDetectorRef) { }

  // ngOnInit() {
  //   this.fetchInductionSteps();
  // }

  // fetchInductionSteps() {
  //   this.service.getWelcome().subscribe({
  //     next: (data) => {
  //       this.onboardingSteps = data.steps;
  //       this.isLoading = false;
  //       this.cdr.detectChanges();
  //     },
  //     error: (error) => {
  //       console.error('Error fetching events:', error);
  //       this.eventsError = 'Unable to load onboarding steps at this time. Please try again later.';
  //       this.isLoading = false;
  //     }
  //   });
  // }
}

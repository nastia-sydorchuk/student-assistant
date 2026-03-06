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
  // onboardingSteps = [
  //   {
  //     order: 1,
  //     summary: 'Online Enrolment',
  //     description: 'Log in to Student Self Service to confirm your personal details, programme of study, and complete official enrolment.',
  //     actionText: 'Go to Student Self Service',
  //     url: 'https://myhwu.hw.ac.uk/',
  //     isSecondary: false
  //   },
  //   {
  //     order: 2,
  //     summary: 'Course & Module Selection',
  //     description: 'Review your mandatory courses and select your optional modules for the upcoming academic year.',
  //     actionText: 'View Course Selection',
  //     url: 'https://www.hw.ac.uk/uk/students/studies/enrolment.htm',
  //     isSecondary: false
  //   },
  //   {
  //     step: 3,
  //     title: 'Tuition Fees & Payments',
  //     description: 'Review your fee status, apply for SAAS/student finance, or set up a payment plan with the university.',
  //     actionText: 'Manage Payments',
  //     url: 'https://www.hw.ac.uk/uk/students/money/pay.htm',
  //     isSecondary: false
  //   },
  //   {
  //     order: 3,
  //     summary: 'Welcome Week Schedule',
  //     description: 'Check the official Welcome Guide for orientation schedules, matriculation events, and Freshers activities.',
  //     actionText: 'View Welcome Guide',
  //     url: 'https://www.hw.ac.uk/uk/students/welcome.htm',
  //     isSecondary: true // We use this to style the button differently
  //   }
  // ];

  onboardingSteps: Step[] = [];
  isLoading = true;
  eventsError = '';

  constructor(private service: Api, private cdr: ChangeDetectorRef) { }

  ngOnInit() {
    this.fetchInductionSteps();
  }

  fetchInductionSteps() {
    this.service.getWelcome().subscribe({
      next: (data) => {
        this.onboardingSteps = data.steps;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error fetching events:', error);
        this.eventsError = 'Unable to load onboarding steps at this time. Please try again later.';
        this.isLoading = false;
      }
    });
  }
}

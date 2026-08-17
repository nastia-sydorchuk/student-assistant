export interface Step {
  order: number;
  summary: string;
  description: string;
  url: string;
  actionText: string;
}

export interface Link {
  label: string;
  url: string;
}

export interface LevelData {
  title: string;
  level: string;
  importantDates: ImportantDate[];
  event: EventDetails; // Optional, because some pages might not have it
}

export interface ImportantDate {
  activity: string;
  date: string;
  type: 'action' | 'deadline' | 'event';
}

export interface RegistrationForm {
  label: string;
  link: string;
}

export interface EventDetails {
  title: string;
  description: string;
  programmeOutline: string[];

  eligibility?: string;       // Used by Postgrad
  eligibilityList?: string[]; // Used by Undergrad
  contactInfo?: string;       // Used by Undergrad
  
  registration: RegistrationForm;
}

export interface InductionEvent {
  title: string;
  icon: string;
  desc: string;
  actionText: string;
  url: string;
}
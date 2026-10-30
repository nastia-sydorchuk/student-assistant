export interface Step {
  order: number;
  summary: string;
  description: string;
  url: string;
  actionText: string;
}

export interface LevelData {
  title: string;
  level: string;
  importantDates: ImportantDate[];
  event: EventDetails;
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

  eligibility: string[];
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
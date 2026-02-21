export interface Step {
  order: number;
  summary: string;
  description: string;
}

export interface Link {
  label: string;
  url: string;
}

export interface WelcomePage {
  steps: Step[];
  source: string;
  cohorts: LevelData[];
}

export interface LevelData {
  title: string;
  importantDates: ImportantDate[];
  event?: EventDetails; // Optional, because some pages might not have it
}

export interface ImportantDate {
  activity: string;
  date: string;
  type: 'action' | 'deadline' | 'event';
}

export interface EventDetails {
  title: string;
  description: string;
  programmeOutline: string[];

  eligibility?: string;       // Used by Postgrad
  eligibilityList?: string[]; // Used by Undergrad
  contactInfo?: string;       // Used by Undergrad
  
  registration: {
    label: string;
    link: string;
  };
}
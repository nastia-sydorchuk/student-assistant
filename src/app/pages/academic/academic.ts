import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-academic',
  imports: [RouterLink],
  templateUrl: './academic.html',
  styleUrl: './academic.css',
})
export class Academic {
  academicResources = [
    {
      title: 'Canvas (VLE)',
      icon: 'laptop_chromebook',
      description: 'Log into Heriot-Watt’s Virtual Learning Environment to access your course materials, lecture slides, and assignment submissions.',
      actionText: 'Log into Canvas',
      url: 'https://canvas.hw.ac.uk/',
      label: 'Every Day',
      badgeClass: 'text-bg-success'
    },
    {
      title: 'Semester Dates',
      icon: 'calendar_month',
      description: 'View important dates, including exam periods, and holidays.',
      actionText: 'View Semester Dates',
      url: 'https://www.hw.ac.uk/study/how-to-apply/academic-calendar',
      label: 'Every Day',
      badgeClass: 'text-bg-success'
    },
    {
      title: 'Timetable',
      icon: 'calendar_month',
      description: 'View your personal timetable and related information.',
      actionText: 'View Timetable',
      url: 'https://timetable.hw.ac.uk/',
      label: 'Every Day',
      badgeClass: 'text-bg-success'
    },
    {
      title: 'The Skills Hub',
      icon: 'psychology',
      description: 'Develop key academic skills including academic writing, critical thinking, referencing, and time management via live workshops.',
      actionText: 'Book a Workshop',
      url: 'https://hw.ac.libcal.com/calendar/skillshub?cid=7632&t=g&d=0000-00-00&cal=7632&inc=0',
      label: 'Skills Development',
      badgeClass: 'text-bg-info'
    },
    {
      title: 'Maths Gym',
      icon: 'calculate',
      description: 'Boost your mathematical and statistical confidence with free drop-in sessions, 1-to-1 appointments, and online resources.',
      actionText: 'Find a Drop-in Session',
      url: 'https://mathsgym.hw.ac.uk/',
      label: 'Skills Development',
      badgeClass: 'text-bg-info'
    },
    {
      title: 'English Gym',
      icon: 'spellcheck',
      description: 'Get support with academic English literacy, including assignment and report writing, editing, and proofreading.',
      actionText: 'Get English Support',
      url: 'http://heriotwatt.sharepoint.com/sites/englishgym',
      label: 'Skills Development',
      badgeClass: 'text-bg-info'
    },
    {
      title: 'LinkedIn Learning',
      icon: 'school',
      description: 'Access a wide range of online courses and training materials to support your academic and professional development.',
      actionText: 'View LinkedIn Learning',
      url: 'https://www.linkedin.com/learning/',
      label: 'Skills Development',
      badgeClass: 'text-bg-info'
    },
    {
      title: 'Library',
      icon: 'local_library',
      description: 'Access print and electronic holdings, book digital study studios, or use the 24/7 Learning Commons and GRID spaces.',
      actionText: 'See How to Borrow',
      url: 'http://hw.ac.uk/about/professional-services/information-services/library-essentials/borrowing-from-hwu-libraries',
      label: 'Every Day',
      badgeClass: 'text-bg-success'
    },
    {
      title: 'Research Databases',
      icon: 'science',
      description: 'Search through thousands of peer-reviewed journals, articles, and publications.',
      actionText: 'Search Discovery',
      url: 'https://discovery.hw.ac.uk/',
      label: 'Research',
      badgeClass: 'text-bg-secondary'
    },
    {
      title: 'Academic Writing consultations',
      icon: 'edit_document',
      description: 'Get specialized assistance with essays, citations.',
      actionText: 'Book a One-to-one Session',
      url: 'https://outlook.office.com/bookwithme/user/9c463d85864243e390132d3577362e1c@hw.ac.uk?anonymous&ep=signature',
      label: 'Skills Development',
      badgeClass: 'text-bg-info'
    }
  ];
}

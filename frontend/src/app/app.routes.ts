import { Routes } from '@angular/router';

export const routes: Routes = [
    { path: '', loadComponent: () => import('./pages/landing/landing').then(m => m.Landing) },
    { path: 'home', loadComponent: () => import('./pages/landing/landing').then(m => m.Landing) },
    { path: 'registration', loadComponent: () => import('./pages/registration/registration').then(m => m.Registration) },
    { path: 'services', loadComponent: () => import('./pages/services/services').then(m => m.Services) },
    { path: 'academic', loadComponent: () => import('./pages/academic/academic').then(m => m.Academic) },
    { path: 'facilities', loadComponent: () => import('./pages/facilities/facilities').then(m => m.Facilities) },
    { path: 'cultural', loadComponent: () => import('./pages/cultural/cultural').then(m => m.Cultural) },
    { path: 'induction', loadComponent: () => import('./pages/induction/induction').then(m => m.Induction) },
    { path: 'wellbeing', loadComponent: () => import('./pages/wellbeing/wellbeing').then(m => m.Wellbeing) },
    { path: 'induction/:level', loadComponent: () => import('./pages/level-induction/level-induction').then(m => m.LevelInduction) },
];

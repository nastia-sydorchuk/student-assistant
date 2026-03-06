import { Routes } from '@angular/router';
import { Landing } from './pages/landing/landing';
import { PostgradInduction } from './pages/postgrad-induction/postgrad-induction';
import { Registration } from './pages/registration/registration';
import { Services } from './pages/services/services';
import { Academic } from './pages/academic/academic';
import { Facilities } from './pages/facilities/facilities';
import { Cultural } from './pages/cultural/cultural';
import { Induction } from './pages/induction/induction';
import { Wellbeing } from './pages/wellbeing/wellbeing';

export const routes: Routes = [
    { path: '', component: Landing },
    { path: 'home', component: Landing },
    { path: 'registration', component: Registration },
    { path: 'services', component: Services },
    { path: 'academic', component: Academic },
    { path: 'facilities', component: Facilities },
    { path: 'cultural', component: Cultural },
    { path: 'induction', component: Induction },
    { path: 'wellbeing', component: Wellbeing },
    { path: 'induction/:level', component: PostgradInduction },
];

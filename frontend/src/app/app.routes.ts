import { Routes } from '@angular/router';
import { adminGuard, authGuard, guestGuard } from './core/auth.guard';
import { DefaultsPage } from './pages/defaults.page';
import { DeliveryPage } from './pages/delivery.page';
import { InvitePage } from './pages/invite.page';
import { KitPage } from './pages/kit.page';
import { LoginPage } from './pages/login.page';
import { PeoplePage } from './pages/people.page';
import { PersonKitPage } from './pages/person-kit.page';
import { TowerPage } from './pages/tower.page';
import { Shell } from './layout/shell';

export const routes: Routes = [
  { path: 'login', component: LoginPage, canActivate: [guestGuard] },
  { path: 'invite/:token', component: InvitePage },
  {
    path: '',
    component: Shell,
    canActivate: [authGuard],
    children: [
      { path: 'app/kit', component: KitPage },
      { path: 'admin/people', component: PeoplePage, canActivate: [adminGuard] },
      { path: 'admin/people/:id', component: PersonKitPage, canActivate: [adminGuard] },
      { path: 'admin/delivery', component: DeliveryPage, canActivate: [adminGuard] },
      { path: 'admin/defaults', component: DefaultsPage, canActivate: [adminGuard] },
      { path: 'admin/tower', component: TowerPage, canActivate: [adminGuard] },
      { path: '', pathMatch: 'full', redirectTo: 'app/kit' }
    ]
  },
  { path: '**', redirectTo: 'login' }
];

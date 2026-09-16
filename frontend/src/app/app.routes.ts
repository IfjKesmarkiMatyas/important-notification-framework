import { Routes } from '@angular/router';
import { adminGuard, authGuard, guestGuard } from './core/auth/auth.guard';
import { DefaultsPage } from './features/defaults/defaults.page';
import { DeliveryPage } from './features/delivery/delivery.page';
import { InvitePage } from './features/auth/invite.page';
import { KitPage } from './features/kit/kit.page';
import { LoginPage } from './features/auth/login.page';
import { PeoplePage } from './features/people/people.page';
import { PersonKitPage } from './features/people/person-kit.page';
import { TowerPage } from './features/tower/tower.page';
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

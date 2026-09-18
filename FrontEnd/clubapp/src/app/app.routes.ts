import {Routes} from '@angular/router';
import {Login} from './Component/login/login';
import {Register} from './Component/register/register';
import {memberGuardGuard} from './Guard/member-guard-guard';
import {Dashboard} from './Component/dashboard/dashboard';
import {ListUserComponent} from './Component/list-user/list-user.component';
import {adminGuard} from './Guard/admin.guard';
import {GestionComponent} from './Component/gestion/gestion.component';
import {UnAuthorizedComponent} from './Component/un-authorized/un-authorized.component';
import {managerGuard} from './Guard/manager.guard';
import {ResponsableDashboardComponent} from './Component/responsable-dashboard/responsable-dashboard.component';
import {ClubMemberComponent} from './Component/club-member-component/club-member-component';
import {TournamentListComponent} from './Component/tournament-list-component/tournament-list-component';
import {FormTournament} from './Component/form-tournament/form-tournament';
import {AdminUserEditComponent} from './Component/admin-user-edit/admin-user-edit.component';
import {ClubListTournament} from './Component/club-list-tournament/club-list-tournament';
import {FormClub} from './Component/form-club/form-club';
import {MemberClubsComponent} from './Component/member-clubs-component/member-clubs-component';
import {ListInvitationComponent} from './Component/list-invitation-component/list-invitation-component';
import {
  MemberTournamentClubListComponenet
} from './Component/member-tournament-club-list-componenet/member-tournament-club-list-componenet';
import {InviationStatusComponent} from './Component/inviation-status-component/inviation-status-component';

export const routes: Routes = [
  {
    path: 'login',
    component: Login,
  },
  {
    path: 'register',
    component: Register,
  },
  { path: 'invitation-result',
    component: InviationStatusComponent,
  },
  {
    path: 'dashboard',
    component: Dashboard,
    canActivate: [memberGuardGuard]
  },
  {
    path: 'my-clubs',
    component: MemberClubsComponent,
    canActivate: [memberGuardGuard]
  },
  {
    path: 'profile-user',
    component: GestionComponent,
    canActivate: [memberGuardGuard]
  },
  {
    path: 'list-tournaments',
    component: TournamentListComponent,
    canActivate: [memberGuardGuard]
  },
  {
    path: 'member/club/tournament/:id',
    component : MemberTournamentClubListComponenet,
    canActivate: [memberGuardGuard]
  },

  // ================== ADMIN ROUTES ==================
  {
    path: 'admin',
    canActivate: [adminGuard],
    children: [
      {
        path: 'users',
        component: ListUserComponent,
      },
      {
        path: 'users/edit/:id',
        component: AdminUserEditComponent,
      },
    ]
  },

  // ================== MANAGER ROUTES ==================
  {
    path: 'responsable',
    canActivate: [managerGuard],
    children: [
      {
        path: 'dashboard',
        component: ResponsableDashboardComponent,
      },
      {
        path: 'club/member/:id',
        component : ClubMemberComponent,
      },
      {
        path: 'club/tournament/:id',
        component : ClubListTournament,
      },
      {
        path: 'club/addClub',
        component: FormClub,
      },
      {
        path: 'club/:id',
        component : FormClub,
      },
      {
        path: 'club/:clubId/addTournament',
        component: FormTournament,
      },
      {
        path: 'club/:clubId/tournament/:tournamentId/invitations',
        component: ListInvitationComponent,
      },
    ]
  },

  {
    path: 'Denied',
    component: UnAuthorizedComponent
  },

  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full',
  },
  {
    path: '**',
    redirectTo: 'login'
  }

]

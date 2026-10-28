import { Routes } from '@angular/router';
import { AuthLoginComponent } from './features/auth-login-component/auth-login-component';
import { DashboardComponent } from './features/dashboard-component/dashboard-component';
import { HeaderComponent } from './features/header-component/header-component'
import { authGuard } from './core/guards/auth-guard';



export const routes: Routes = [
    { path: 'login', component: AuthLoginComponent},
    {
      path: '',
      component: HeaderComponent,
      canActivate: [authGuard],
      children: [
        { path: 'dashboard', component: DashboardComponent },
      ]
    }


];

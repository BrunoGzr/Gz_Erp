import { Routes } from '@angular/router';
import { AuthLoginComponent } from './features/auth-login-component/auth-login-component';
import { DashboardComponent } from './features/dashboard-component/dashboard-component';



export const routes: Routes = [
    { path: 'login', component: AuthLoginComponent},
    { path: '', redirectTo: 'dashboard', pathMatch: 'full'},
    { path: "dashboard", component: DashboardComponent}

];

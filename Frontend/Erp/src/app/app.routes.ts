import { Routes } from '@angular/router';
import { AuthLoginComponent } from '../features/auth-login-component/auth-login-component';


export const routes: Routes = [
    { path: 'login', component: AuthLoginComponent},
    { path: '', redirectTo: 'login', pathMatch: 'full'}
];

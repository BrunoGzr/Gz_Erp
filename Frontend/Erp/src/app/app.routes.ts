import { Routes } from '@angular/router';
import { AuthLoginComponent } from './features/auth-login-component/auth-login-component';
import { DashboardComponent } from './features/dashboard-component/dashboard-component';
import { HeaderComponent } from './features/header-component/header-component'
import { authGuard } from './core/guards/auth-guard';
import { ProductsComponents } from './features/products-component/products-components';



export const routes: Routes = [
    { path: 'login', component: AuthLoginComponent},
    {
      path: '',
      component: HeaderComponent,
      canActivate: [authGuard],
      children: [
        { path: 'dashboard', component: DashboardComponent },
        { path: "", redirectTo: 'dashboard', pathMatch: 'full' },
        {
          path: 'produtos/novo',
          loadComponent: () =>
            import('./features/product-form-component/product-form-component').then(
              (module) => module.ProductFormComponent,
            ),
        },
        {
          path: 'produtos/:publicId/editar',
          loadComponent: () =>
            import('./features/product-form-component/product-form-component').then(
              (module) => module.ProductFormComponent,
            ),
        },
        {
          path: 'produtos/:publicId',
          loadComponent: () =>
            import('./features/product-detail-component/product-detail-component').then(
              (module) => module.ProductDetailComponent,
            ),
        },
        { path: 'produtos', component: ProductsComponents}
      ]
    },
];

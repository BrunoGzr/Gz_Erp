import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../../service/auth-login-service';
import { catchError, map, of } from 'rxjs';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const token = authService.getAccesToken();

  if (!authService.getAccesToken()) {
    router.navigate(['/login']);
    return false
  }

  if (authService.isTokenValid()){
    return true;
  }

  return authService.refreshToken().pipe(
  map(() => true),
    catchError(()=> {
      authService.logout();
      router.navigate(['/login'], {queryParams: {returnUrl: state.url}});
      return of(false);
    })
  );
};

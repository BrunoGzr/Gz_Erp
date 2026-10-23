import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../../service/auth-login-service';
import { Router } from '@angular/router';
import tr from '@angular/common/locales/tr';
import { catchError, switchMap, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {


  const authService = inject(AuthService)
  const router = inject(Router)

  const token = authService.getAccesToken();

  let authReq = req.clone({
    withCredentials: true
  });

  if(token && !req.url.includes("/login") && !req.url.includes("/refresh")){
    authReq = authReq.clone({
      setHeaders: {Authorization: `Bearer ${token}` }
    });
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !req.url.includes("/login") && !req.url.includes("/refresh")){
        return authService.refreshToken().pipe(
          switchMap((newResponse) => {
            const newReq = req.clone({
              withCredentials: true,
              setHeaders: {Authorization: `Bearer ${newResponse.token}`}
            });
            return next(newReq);
          }),
          catchError((refreshError) => {
            authService.logout();
            router.navigate(['/login']);
            return throwError(() => refreshError);
          })
        );
      }

      return throwError(() => error);
    })
  )



    
};

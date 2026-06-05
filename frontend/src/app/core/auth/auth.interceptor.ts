import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { Auth } from '../services/auth';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(Auth);
  const router = inject(Router);
  const token = auth.getAccessToken();

  const isAuthRoute =
    req.url.includes('/auth/login') ||
    req.url.includes('/auth/refresh');
  const isPublicRoute =
    isAuthRoute ||
    req.url.includes('/rutas/transferencias-disponibles') ||
    req.url.includes('/inventario/transferencias');

  const authReq =
    token && !isPublicRoute
      ? req.clone({
          setHeaders: {
            Authorization: `Bearer ${token}`,
          },
        })
      : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      const refreshToken = auth.getRefreshToken();
      const shouldRefreshSession = error.status === 401 && refreshToken && !isAuthRoute;

      if (shouldRefreshSession) {
        return auth.refresh(refreshToken).pipe(
          switchMap((response) => {
            auth.saveSession(response);

            return next(
              req.clone({
                setHeaders: {
                  Authorization: `Bearer ${response.accessToken}`,
                },
              })
            );
          }),
          catchError((err) => {
            auth.clearSession();
            void router.navigate(['/']);
            return throwError(() => err);
          })
        );
      }

      if (error.status === 401 && !isAuthRoute) {
        auth.clearSession();
        void router.navigate(['/']);
      }

      return throwError(() => error);
    })
  );
};

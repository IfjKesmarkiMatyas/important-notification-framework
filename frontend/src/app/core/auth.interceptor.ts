import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const token = auth.token();
  const isAuthCall = req.url.startsWith('/api/auth/');
  const nextReq =
    token && !isAuthCall ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;
  return next(nextReq).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && !isAuthCall) {
        auth.clear();
        void router.navigateByUrl('/login');
      }
      return throwError(() => err);
    })
  );
};

export function apiError(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    const body = err.error as { error?: string; message?: string } | string | null;
    if (typeof body === 'string' && body.trim()) {
      return body;
    }
    if (body && typeof body === 'object') {
      return body.error || body.message || err.message;
    }
    return err.message;
  }
  return 'Request failed';
}

import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Auth } from '../services/auth';

export const authGuard: CanActivateFn = () => {
  const auth = inject(Auth);
  const router = inject(Router);

  if (!auth.getSession() || auth.isSessionExpired()) {
    auth.clearSession();
    void router.navigate(['/']);
    return false;
  }

  return true;
};

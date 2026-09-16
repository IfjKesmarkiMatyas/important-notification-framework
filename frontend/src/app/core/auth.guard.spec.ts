import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { adminGuard, authGuard, guestGuard } from './auth.guard';
import { AuthService } from './auth.service';
import { SessionUser } from './models';

describe('route guards', () => {
  const admin: SessionUser = {
    token: 'jwt',
    id: '1',
    email: 'admin@notif.local',
    displayName: 'Admin',
    role: 'ADMIN',
    status: 'ACTIVE'
  };

  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    localStorage.clear();
    router = jasmine.createSpyObj('Router', ['createUrlTree']);
    router.createUrlTree.and.callFake((commands: unknown[]) => ({ commands }) as unknown as UrlTree);
    TestBed.configureTestingModule({
      providers: [AuthService, { provide: Router, useValue: router }]
    });
  });

  it('authGuard redirects anonymous users to login', () => {
    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as never, {} as never)
    );
    expect(router.createUrlTree).toHaveBeenCalledWith(['/login']);
    expect(result).toEqual({ commands: ['/login'] } as unknown as UrlTree);
  });

  it('guestGuard sends logged-in users to kit', () => {
    TestBed.inject(AuthService).setSession(admin);
    const result = TestBed.runInInjectionContext(() =>
      guestGuard({} as never, {} as never)
    );
    expect(router.createUrlTree).toHaveBeenCalledWith(['/app/kit']);
    expect(result).toEqual({ commands: ['/app/kit'] } as unknown as UrlTree);
  });

  it('adminGuard sends non-admins to kit', () => {
    TestBed.inject(AuthService).setSession({ ...admin, role: 'USER' });
    TestBed.runInInjectionContext(() => adminGuard({} as never, {} as never));
    expect(router.createUrlTree).toHaveBeenCalledWith(['/app/kit']);
  });

  it('adminGuard allows admins', () => {
    TestBed.inject(AuthService).setSession(admin);
    const result = TestBed.runInInjectionContext(() =>
      adminGuard({} as never, {} as never)
    );
    expect(result).toBeTrue();
  });
});

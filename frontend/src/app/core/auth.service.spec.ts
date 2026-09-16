import { AuthService } from './auth.service';
import { SessionUser } from './models';

describe('AuthService', () => {
  const session: SessionUser = {
    token: 'jwt',
    id: '1',
    email: 'admin@notif.local',
    displayName: 'Admin',
    role: 'ADMIN',
    status: 'ACTIVE'
  };

  beforeEach(() => localStorage.clear());

  it('stores and clears the session', () => {
    const auth = new AuthService();
    expect(auth.isLoggedIn()).toBeFalse();
    auth.setSession(session);
    expect(auth.isLoggedIn()).toBeTrue();
    expect(auth.isAdmin()).toBeTrue();
    expect(auth.token()).toBe('jwt');
    expect(JSON.parse(localStorage.getItem('notif.session')!).email).toBe('admin@notif.local');
    auth.clear();
    expect(auth.isLoggedIn()).toBeFalse();
    expect(localStorage.getItem('notif.session')).toBeNull();
  });

  it('treats USER role as non-admin', () => {
    const auth = new AuthService();
    auth.setSession({ ...session, role: 'USER' });
    expect(auth.isAdmin()).toBeFalse();
  });
});

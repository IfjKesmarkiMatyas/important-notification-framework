export type UserRole = 'USER' | 'ADMIN';
export type UserStatus = 'INVITED' | 'ACTIVE' | 'INACTIVE' | 'REVOKED';

export interface SessionUser {
  token: string;
  id: string;
  email: string;
  displayName: string | null;
  role: UserRole;
  status: UserStatus;
}

export interface UserView {
  id: string;
  email: string;
  displayName: string | null;
  role: UserRole;
  status: UserStatus;
  createdAt: string;
  lastInviteStatus: string | null;
  lastInviteError: string | null;
  lastInviteAt: string | null;
}

export interface InvitePeek {
  email: string;
}

export interface InviteResponse {
  userId: string;
  deliveryJobId: string;
}

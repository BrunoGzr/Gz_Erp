export type LoginRequest = {
  identifier: string;
  password: string;
};

export interface LoginResponse {
  token: string;
  type: 'Bearer';
  expirationInSec: number;
  refreshToken: string;
  userPublicId: string;
  tenantId: number;
  userType: UserType;
}

export interface RefreshTokenResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface User {
  companyName: string;
  name: string;
  username?: string;
  profilePicture?: string | null;
  id: number;
  email: string;
  tenantId: number;
  userType: string;
}

type UserType = 'PARTNER' | 'EMPLOYEE' | 'ADMIN';

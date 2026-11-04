import { HttpClient } from '@angular/common/http';
import { inject, Service, signal } from '@angular/core';
import { LoginRequest, LoginResponse, RefreshTokenResponse, User } from '../models/loginModels';
import { Observable, tap, throwError } from 'rxjs';
import { jwtDecode } from 'jwt-decode';

@Service()
export class AuthService {
  currentUser = signal<User | null>(null);

  private http = inject(HttpClient);
  private apiUrl = '/api/auth';

  constructor() {
    this.loadUserFromToken();
  }

  requestLogin(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${this.apiUrl}/login`, credentials, { withCredentials: true })
      .pipe(
        tap((response) => {
          this.storeTokens(response.token);
          this.loadUserFromToken();
        }),
      );
  }

  refreshToken(): Observable<RefreshTokenResponse> {

    return this.http
      .post<RefreshTokenResponse>(
        `${this.apiUrl}/refresh`,
            {},
            { withCredentials: true },
      )
      .pipe(
        tap((response) => {
          this.storeTokens(response.accessToken);
          this.loadUserFromToken();
        }),
      );
  }

  getAccesToken(): string | null {
    return localStorage.getItem('access_token');
  }

  logout(): void {
    const refreshToken = localStorage.getItem('refresh_token');

    this.clearSession();

    if (refreshToken) {
      this.http
        .post<void>(`${this.apiUrl}/logout`, { refreshToken }, { withCredentials: true })
        .subscribe({ error: () => undefined });
    }
  }

  private storeTokens(accessToken: string): void {
    localStorage.setItem('access_token', accessToken);
  }

  private clearSession(): void {
    localStorage.removeItem('access_token');
    this.currentUser.set(null);
  }

  private loadUserFromToken(): void {
    const token = localStorage.getItem('access_token');

    if (!token) {
      this.currentUser.set(null);
      return;
    }

    try {
      const decoded: any = jwtDecode(token);

      this.currentUser.set({
        companyName: decoded.companyName ?? decoded.tenantName ?? decoded.nomeFantasia ?? 'Empresa',
        name:
          decoded.name ??
          decoded.fullName ??
          decoded.username ??
          decoded.sub ??
          decoded.email ??
          'Usuário',
        username: decoded.sub ?? decoded.username ?? '',
        profilePicture: decoded.profilePicture ?? decoded.avatarUrl ?? decoded.picture ?? null,
        id: decoded.id,
        email: decoded.email,
        tenantId: decoded.tenantId,
        userType: decoded.userType,
      });
    } catch {
      this.currentUser.set(null);
    }
  }

  isTokenValid(): boolean {
    const token = localStorage.getItem('access_token');

    if (!token) {
      return false;
    }

    try {
      const decoded: any = jwtDecode(token);
      const expiryDate = decoded['exp'] * 1000;
      const now = Date.now();

      return expiryDate > now + 10000;
    } catch {
      return false;
    }
  }
}

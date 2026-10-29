import { HttpClient } from '@angular/common/http';
import { inject, Service, signal } from '@angular/core';
import { LoginRequest, LoginResponse, User } from '../models/loginModels';
import { Observable, tap } from 'rxjs';
import { jwtDecode } from 'jwt-decode';
import { routes } from '../app.routes';


@Service()
export class AuthService {

    currentUser = signal<User | null>(null);

    private http = inject(HttpClient)



    private apiUrl = "/api/auth";



  constructor() {
      this.loadUserFromToken();
  }

    requestLogin(credentials: LoginRequest): Observable<any>{

        return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials, {withCredentials: true}).pipe(
            tap(Response => {
                localStorage.setItem('access_token', Response.token);
                this.loadUserFromToken();
            })
        )
    };

    refreshToken(): Observable<any> {

        return this.http.post<any>(`${this.apiUrl}/refresh`, {}, {withCredentials: true}).pipe(
            tap(Response => {
                console.log('Token a ser salvo:', Response.accessToken);
                localStorage.setItem('access_token', Response.accessToken);

            })
        )
    }

    getAccesToken(): string | null {
        return localStorage.getItem('access_token');
    }

    logout(){
        localStorage.removeItem('access_token');
        this.currentUser.set(null);
    }

    private loadUserFromToken() {
      const token = localStorage.getItem('access_token');

      if (token) {
        try {
          const decoded: any = jwtDecode(token);

          this.currentUser.set({
            companyName: decoded.companyName,
            name: decoded.name,
            id: decoded.id,
            email: decoded.email,
            tenantId: decoded.tenantId,
            userType: decoded.userType,
          });
        } catch (e) {
          this.currentUser.set(null);
        }
      }else {
        this.currentUser.set(null);
      }
    }

    isTokenValid(): boolean  {
      const token = localStorage.getItem('access_token');
      if (!token) {
        return false;
      }

      try {
        const decoded: any = jwtDecode(token);
        const expiryDate = decoded['exp'] * 1000;
        const now = Date.now();

        return expiryDate > (now + 10000);
      }catch (e) {
        return false;
      }


    }

}

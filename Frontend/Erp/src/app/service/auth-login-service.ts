import { HttpClient, provideHttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { LoginRequest, LoginResponse } from '../models/loginModels';
import { Observable, tap } from 'rxjs';


@Service()
export class AuthService {

    private http = inject(HttpClient)

    private apiUrl = "http://192.168.15.23:8989/api/auth"

    requestLogin(credentials: LoginRequest): Observable<any>{

        return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials).pipe(
            tap(Response => {
                localStorage.setItem('access_token', Response.token);
            })
        )
    };

    refreshToken(): Observable<any> {

        return this.http.post<any>('${apiUrl}/refresh', {}, {withCredentials: true}).pipe(
            tap(Response => {
                localStorage.setItem('access_token', Response.token);
            })
        )
    }

    getAccesToken(): string | null {
        return localStorage.getItem('access_token');
    }

    logout(){
        localStorage.removeItem('access_token');
    }

    
}

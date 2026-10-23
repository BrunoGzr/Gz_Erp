import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { LoginRequest, LoginResponse } from '../models/loginModels';


@Service()
export class AuthLoginService {
    private http = inject(HttpClient)

    private apiUrl = "http://192.168.15.23:8989/api/auth"

    requestLogin(credentials: LoginRequest){
        return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials)
    }

    
}

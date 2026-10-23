import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthLoginService } from '../../core/service/auth-login-service';
import { identity } from 'rxjs';
import { Router } from '@angular/router';

@Component({
  imports: [FormsModule],
  selector: 'app-auth-login-component',
  styleUrl: './auth-login-component.css',
  templateUrl: './auth-login-component.html',
})
export class AuthLoginComponent {
  username = "";
  password = "";


  private authService = inject(AuthLoginService)
  private router = inject(Router)

  doLogin(){
    if(!this.username || !this.password){
      alert('Preencha todos os campos.')
    }

    const credentials = {
      identifier: this.username,
      password: this.password
    }

    this.authService.requestLogin(credentials).subscribe({
      next: (Response) => {
        console.log('Login deu certo zé', Response);

        localStorage.setItem('access_token', Response.token);

        this.router.navigate(['']);

      },
      error: (err) => {
        console.error('erro no loggin', err);
      },

    })









    
  }

}

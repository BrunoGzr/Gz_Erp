import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../service/auth-login-service';

@Component({
  imports: [FormsModule],
  selector: 'app-auth-login-component',
  styleUrl: './auth-login-component.css',
  templateUrl: './auth-login-component.html',
})
export class AuthLoginComponent {
  username = "";
  password = "";

  errorMessage = signal<string | null>(null);
  isLoading = signal<boolean>(false);


  private authService = inject(AuthService)
  private router = inject(Router)
  private destroyRef = inject(DestroyRef)

  doLogin(){

    this.errorMessage.set(null);

    if(!this.username || !this.password){
      this.errorMessage.set("Preencha todos os campos para fazer o Login")
      return;
    }

    if (this.isLoading()) {
      return;
    }

    const credentials = {
      identifier: this.username,
      password: this.password
    }

    this.isLoading.set(true);

    this.authService.requestLogin(credentials)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.isLoading.set(false);
          this.router.navigate(['/dashboard']);
        },
        error: (err: HttpErrorResponse) => {
          this.isLoading.set(false);

          if (err.status == 401){
            this.errorMessage.set("Usuario ou senha invalidos.")
          } else if (err.status == 0) {
            this.errorMessage.set("Não foi possivel conectar ao servidor, verifique sua conexão com a internet e tente novamente.")
          } else {
            this.errorMessage.set("Ocorreu um erro inesperado, tente novamente mais tarde.")
          }
        },
      })

  }

}

import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { AuthService } from '../../service/auth-login-service';
import { NgOptimizedImage } from '@angular/common';

@Component({
  imports: [RouterLink, RouterOutlet, NgOptimizedImage],
  selector: 'app-header-component',
  styleUrl: './header-component.css',
  templateUrl: './header-component.html',
})
export class HeaderComponent {
  public authservice = inject(AuthService);
  private router = inject(Router);

  profilePicture =
    'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS67I-cDIedQO7iBV4XjX8T22NC7hNznLv0JhRNbzToURaR5vZna42TO_w&s=10';

  firstName = 'algo';

  companyName = 'Algo da silva zé';

  doLogout() {
    this.authservice.logout();
    this.router.navigate(['/']);
  }
}

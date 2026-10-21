import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  imports: [FormsModule],
  selector: 'app-auth-login-component',
  styleUrl: './auth-login-component.css',
  templateUrl: './auth-login-component.html',
})
export class AuthLoginComponent {
  username = "";
  password = "";

  doLogin(){
    console.log("Username é : " + this.username);
    console.log("password é : " + this.password);
    
  }

}

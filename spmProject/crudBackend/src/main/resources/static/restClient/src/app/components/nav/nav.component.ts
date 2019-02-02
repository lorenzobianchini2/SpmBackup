import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../auth.service';

@Component({
  selector: 'app-nav',
  templateUrl: './nav.component.html',
  styleUrls: ['./nav.component.css']
})
export class NavComponent implements OnInit {
  isLoggedIn: boolean;
  id: string;
  today = new Date();
  dateNow;
  dateLogin;

  constructor(private _rotuer: Router, public authService: AuthService) { }
  ngOnInit() {
    this.dateLogin = localStorage.getItem('dateLogin');
    if (this.dateLogin != null) {
      this.dateNow = this.today.getTime();
      if ((this.dateNow - parseInt(this.dateLogin, 10)) > 28800000) {
        localStorage.setItem('dateLogin', null);
        this.logout();
      } else {
        this.dateLogin = this.today.getTime();
        localStorage.setItem('dateLogin', this.dateLogin);
      }
    }
    if (localStorage.getItem('isLoggedIn') === 'true') {
      this.isLoggedIn = true;
    } else {
      this.isLoggedIn = false;
    }
    this.id = localStorage.getItem('token');
  }

  logout(): void {
    this.authService.logout();
    this.isLoggedIn = false;
    this._rotuer.navigate(['/']);
    window.location.reload();
  }
}
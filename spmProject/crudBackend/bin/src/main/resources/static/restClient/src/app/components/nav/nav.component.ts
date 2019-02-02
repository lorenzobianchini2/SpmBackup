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

  constructor(private _rotuer:Router, public authService: AuthService) { }

  ngOnInit() {
    if(localStorage.getItem('isLoggedIn')=="true") {
      this.isLoggedIn=true;
    } else {
      this.isLoggedIn=false;
    }
    this.id = localStorage.getItem('token');
  }
  logout(): void {
    this.authService.logout();
    this.isLoggedIn=false;
    this._rotuer.navigate(['/']);
  }
}

import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../auth.service';
import { UserService } from '../../shared_service/user.service';
import { User } from '../../user';

@Component({
  selector: 'app-user-home',
  templateUrl: './user-home.component.html',
  styleUrls: ['./user-home.component.css']
})
export class UserHomeComponent implements OnInit {
  id: string;
  private user: User;

  constructor(private _userService: UserService, private router: Router, private authService: AuthService) { }

  ngOnInit() {
    this.id = localStorage.getItem('token');
    this.user = this._userService.getter();
  }

  createFolder(folderName) {
    this._userService.createFolder(this.user.username, folderName).subscribe(() => {

    });
  }
}

import { Component, OnInit, SystemJsNgModuleLoader } from '@angular/core';
import { User } from '../../user';
import { Router } from '@angular/router';
import { UserService } from '../../shared_service/user.service';
import { AuthService } from '../../auth.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-user-form',
  templateUrl: './user-form.component.html',
  styleUrls: ['./user-form.component.css']
})
export class UserFormComponent implements OnInit {
  id: string;
  success: boolean;
  existingEmail: String = '';
  private user: User;
  private users: User[];
  messageForm: FormGroup;
  submitted = false;
  alert = false;
  today = new Date();
  dateLogin;
  i = 0;

  constructor(private _userService: UserService, private _rotuer: Router, public authService: AuthService, private formBuilder: FormBuilder) { }
  ngOnInit() {
    this.id = localStorage.getItem('token');
    if (this.id !== null) {
      this._rotuer.navigate(['/hp']);
    }
    this.existingEmail = '';
    this.messageForm = this.formBuilder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
    this.user = this._userService.getter();
    this.user.username = null;
    this.user.password = null;
  }

  processForm() {
    this.alert = false;
    this.submitted = true;
    this.success = false;
    if (this.messageForm.invalid) {
      return;
    }
    this._userService.loginUser(this.user).subscribe((success) => {
      if (success) {
        this._userService.getUsers().subscribe((users) => {
          this.users = users;
          for (this.i = 0; this.i < users.length; this.i++) {
            if (this.user.username === this.users[this.i].username) {
              localStorage.setItem('isLoggedIn', 'true');
              localStorage.setItem('token', this.user.username);
              this._rotuer.navigate(['/hp']);
            } else if (this.user.username === this.users[this.i].email) {
              localStorage.setItem('isLoggedIn', 'true');
              localStorage.setItem('token', this.users[this.i].username);
              this._rotuer.navigate(['/hp']);
            }
          }
        });
        this.dateLogin = this.today.getTime();
        localStorage.setItem('dateLogin', this.dateLogin);
      } else {
        this.existingEmail = 'Wrong username or password!';
        this.alert = true;
        return;
      }
    });
  }
}
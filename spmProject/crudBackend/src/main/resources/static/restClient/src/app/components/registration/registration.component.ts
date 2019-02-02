import { Component, OnInit } from '@angular/core';
import { User } from '../../user';
import { Folder } from '../../folder';
import { Router } from '@angular/router';
import { UserService } from '../../shared_service/user.service';
import { AuthService } from '../../auth.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-registration',
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.css']
})
export class RegistrationComponent implements OnInit {
  today = new Date();
  dd = this.today.getDate();
  mm = this.today.getMonth();
  yyyy = this.today.getFullYear() - 15;
  date = this.yyyy + '-' + this.mm + '-' + this.dd;
  id: string;
  existingUsername: String = '';
  i = 0;
  userId = 0;
  private user: User;
  private users: User[];
  private folders: Folder[];
  messageForm: FormGroup;
  submitted = false;
  success = false;
  alert = false;

  constructor(private formBuilder: FormBuilder, private _userService: UserService, private _rotuer: Router, public authService: AuthService) { }
  ngOnInit() {
    this.id = localStorage.getItem('token');
    if (this.id != null) {
      this._rotuer.navigate(['/hp']);
    }
    this.existingUsername = '';
    this.user = this._userService.getter();
    this.messageForm = this.formBuilder.group({
      usernameReg: ['', Validators.required],
      passwordReg: ['', Validators.required],
      repasswordReg: ['', Validators.required],
      affiliation: ['', Validators.required],
      fname: ['', Validators.required],
      lname: ['', Validators.required],
      email: ['', Validators.required],
      date: ['', Validators.required]
    });
    this.user.username = null;
    this.user.password = null;
    this.user.affiliation = null;
    this.user.name = null;
    this.user.surname = null;
    this.user.email = null;
    this.user.date_of_birth = null;
  }

  processForm() {
    this.userId = 0;
    this.existingUsername = '';
    this.submitted = true;
    this.alert = false;
    if (this.messageForm.invalid) {
      return;
    }
    if (this.user.password !== this.user.repassword) {
      this.existingUsername = 'Password do not match!';
      this.alert = true;
      return;
    }
    this._userService.getUsers().subscribe((users) => {
      this.users = users;
      for (this.i = 0; this.i < users.length; this.i++) {
        if (this.users[this.i].email === this.user.email) {
          this.existingUsername = 'This E-Mail already exists!';
          this.alert = true;
          return;
        }
      }
      for (this.i = 0; this.i < users.length; this.i++) {
        if (this.userId < (this.users[this.i].id)) {
          this.userId = Number(this.users[this.i].id);
        }
      }
      this.user.id = this.userId + 1;
      this.success = true;
      this._userService.createUser(this.user).subscribe((user) => {
        localStorage.setItem('isLoggedIn', 'true');
        localStorage.setItem('token', this.user.username);
        this._rotuer.navigate(['/hp']);
      });
    });
  }
}
import { Component, OnInit } from '@angular/core';
import { User } from '../../user';
import { UserService } from '../../shared_service/user.service';
import { AuthService } from '../../auth.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-user-data',
  templateUrl: './user-data.component.html',
  styleUrls: ['./user-data.component.css']
})
export class UserDataComponent implements OnInit {
  id: string;
  existingUsername: String;
  success: String;
  private user: User;
  private users: User[];
  messageForm: FormGroup;
  submitted = false;
  alert = false;
  done = false;
  i = 0;

  constructor(private formBuilder: FormBuilder, private _userService: UserService, public authService: AuthService) { }
  ngOnInit() {
    this.alert = false;
    this.done = false;
    this.id = localStorage.getItem('token');
    this.user = this._userService.getter();
    this.messageForm = this.formBuilder.group({
      password: ['', Validators.required],
      repassword: ['', Validators.required]
    });
    this.user.password = null;
    this.user.repassword = null;
  }

  processForm(password, repassword) {
    this.submitted = true;
    this.existingUsername = '';
    if (this.messageForm.invalid) {
      return;
    }
    if (password !== repassword) {
      this.existingUsername = 'Password do not match!';
      this.alert = true;
      this.done = false;
      return;
    }
    this._userService.getUsers().subscribe((users) => {
      this.users = users;
      for (this.i = 0; this.i < users.length; this.i++) {
        if (this.users[this.i].username === this.id) {
          this.user = this.users[this.i];
          this._userService.updateUser(this.user, password).subscribe((exists) => {
            if (exists) {
              this.existingUsername = 'The new password can not be the same as the old one!';
              this.alert = true;
              this.done = false;
              return;
            } else {
              this.success = 'The password has been changed successfully!';
              this.done = true;
              this.alert = false;
              this.user.password = null;
              this.user.repassword = null;
              return;
            }
          });
        }
      }
    });
  }
}
import { Component, OnInit } from '@angular/core';
import { UserService } from '../../shared_service/user.service';
import { User } from '../../user';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent implements OnInit {
  alert = false;
  done = true;
  private user: User;
  private users: User[];
  messageForm: FormGroup;
  submitted = false;
  existingUsername: String = '';
  password: String = '';
  i = 0;
  x = 0;
  chars: String;
  pass: String;

  constructor(private formBuilder: FormBuilder, private _userService: UserService) { }
  ngOnInit() {
    this.done = false;
    this.alert = false;
    this.user = this._userService.getter();
    this.messageForm = this.formBuilder.group({
      email: ['', Validators.required]
    });
    this.user.email = '';
  }

  randomPassword() {
    this.chars = 'abcdefghijklmnopqrstuvwxyz!?ABCDEFGHIJKLMNOP1234567890';
    this.pass = '';
    for (this.x = 0; this.x < 8; this.x++) {
      this.i = Math.floor(Math.random() * this.chars.length);
      this.pass += this.chars.charAt(this.i);
    }
    return this.pass;
  }

  recoveryPassword(email) {
    this.existingUsername = '';
    this.submitted = true;
    if (this.messageForm.invalid) {
      return;
    }
    this.password = this.randomPassword();
    this._userService.getUsers().subscribe((users) => {
      this.users = users;
      for (this.i = 0; this.i < users.length; this.i++) {
        if (this.users[this.i].email === email) {
          this.user = users[this.i];
          this._userService.sendMail(this.user, this.password).subscribe(() => { });
          this.existingUsername = 'E-Mail sent!';
          this.done = true;
          this.alert = false;
          return;
        }
      }
      this.existingUsername = 'This E-Mail address does not exist!';
      this.done = false;
      this.alert = true;
      return;
    });
  }
}
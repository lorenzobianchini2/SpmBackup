import { Component, OnInit } from '@angular/core';
import { UserService } from '../../shared_service/user.service';
import { Router } from '@angular/router';
import { User } from '../../user';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent implements OnInit {

  private user: User;
  private users: User[];
  messageForm: FormGroup;
  submitted = false;
  existingUsername: string = "";

  constructor(private formBuilder: FormBuilder, private _userService: UserService,
    private _rotuer: Router) { }

  ngOnInit() {
    this.user = this._userService.getter();
    this.messageForm = this.formBuilder.group({
      email: ['', Validators.required]
    });
    this.user.email = "";
  }
  randomPassword() {
    var chars = "abcdefghijklmnopqrstuvwxyz!?ABCDEFGHIJKLMNOP1234567890";
    var pass = "";
    for (var x = 0; x < 8; x++) {
      var i = Math.floor(Math.random() * chars.length);
      pass += chars.charAt(i);
    }
    return pass;
  }
  recoveryPassword(email) {

    this.existingUsername = "";
    this.submitted = true;
    if (this.messageForm.invalid) {
      return;
    }
    var password = this.randomPassword();
    this._userService.getUsers().subscribe((users) => {
      this.users = users;
      for (var i = 0; i < users.length; i++) {
        if (this.users[i].email == email) {
          this.user = users[i];
          this._userService.sendMail(this.user, password).subscribe(() => {

          });
          this.existingUsername = "E-Mail sent!";

          return;
        }
      }
      this.existingUsername = "This E-Mail address does not exist!";
      return;
    });
  }
}
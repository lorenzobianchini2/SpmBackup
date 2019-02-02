import { Component, OnInit } from '@angular/core';
import { User } from '../../user';
import { Router } from '@angular/router';
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
  private user: User;
  private users: User[];
  messageForm: FormGroup;
  submitted = false;
  success = false;
  present = false;

  constructor(private formBuilder: FormBuilder, private _userService: UserService, private _rotuer: Router, public authService: AuthService) { }
  ngOnInit() {

    this.id = localStorage.getItem('token');
    this.user = this._userService.getter();
    this.messageForm = this.formBuilder.group({
      password: ['', Validators.required],
      repassword: ['', Validators.required]
    });
  }
  processForm(password, repassword) {
    this.submitted = true;
    this.existingUsername = "";
    if (this.messageForm.invalid) {
      return;
    }


    if (password != repassword) {
      this.existingUsername = "Password do not match!";
      return;
    }

    this._userService.getUsers().subscribe((users) => {
      this.users = users;
      for (var i = 0; i < users.length; i++) {

        if (this.users[i].username == this.id) {
          this.user = this.users[i];
          this._userService.updateUser(this.user, password).subscribe((exists) => {
            if (exists) {
              this.existingUsername = "The new password can not be the same as the old one!";
            }
            else {
              this.existingUsername = "The password has been changed successfully!";
            }

          });

        }
      }
    });
    this.existingUsername = "";
  }
}

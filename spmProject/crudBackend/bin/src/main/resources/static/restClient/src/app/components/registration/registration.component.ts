import { Component, OnInit } from '@angular/core';
import { User } from '../../user';
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
  id: string;
  existingUsername : string ="";
  private user: User;
  private users: User[];
  messageForm: FormGroup;
  submitted = false;
  success = false;
  constructor(private formBuilder: FormBuilder, private _userService:UserService, private _rotuer:Router, public authService: AuthService) {}
  ngOnInit() {
    this.id = localStorage.getItem('token');
    if(this.id != null) {
      this._rotuer.navigate(['/hp']);
    }
    this.user = this._userService.getter();
    this.messageForm = this.formBuilder.group({
      usernameReg: ['', Validators.required],
      passwordReg: ['', Validators.required],
      affiliation: ['', Validators.required],
      fname: ['', Validators.required],
      lname: ['', Validators.required],
      email: ['', Validators.required],
      date: ['', Validators.required]
    });
    this.user.username=null;
    this.user.password=null;
    this.user.affiliation=null;
    this.user.name=null;
    this.user.surname=null;
    this.user.email=null;
    this.user.date_of_birth=null;
  }
  processForm() {
    var userId = 0;
    this.existingUsername="";
    this.submitted = true;
    if (this.messageForm.invalid) {
      return;
    }
    this._userService.getUsers().subscribe((users)=>{
      
      this.users = users;
      for(var i=0;i<users.length;i++){
        if(this.users[i].email == this.user.email){
          this.existingUsername = "This E-Mail already exists!";
           return;
        }
        if(this.users[i].username == this.user.username){
          this.existingUsername = "This Username already exists!";
           return;
        }
      }
      for(var i=0;i<users.length;i++){
        if(userId < (this.users[i].id))
        userId = Number(this.users[i].id);
      }
      this.user.id = userId+1;
      
      this.success = true;
      this._userService.createUser(this.user).subscribe((user) => {
        localStorage.setItem('isLoggedIn', "true");
        localStorage.setItem('token', this.user.username);
        this._rotuer.navigate(['/hp']);
      });
    });
  }
}

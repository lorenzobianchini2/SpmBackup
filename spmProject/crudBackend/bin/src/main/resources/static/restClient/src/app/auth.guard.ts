import { Injectable } from '@angular/core';
import { CanActivate, CanActivateChild, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { AuthService } from './auth.service';

@Injectable()

export class AuthGuard implements CanActivate {
  
  constructor(private auth: AuthService, private _rotuer:Router){}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    let url: string = state.url;  
    return this.verifyLogin(url);
}

verifyLogin(url) : boolean{
    if(!this.isLoggedIn()){
        this._rotuer.navigate(['/']);
        return false;
    }
    else if(this.isLoggedIn()){
        return true;
    }
}
public isLoggedIn(): boolean{
    let status = false;
    if( localStorage.getItem('isLoggedIn') == "true"){
      status = true;
    }
    else{
      status = false;
    }
    return status;
}
}

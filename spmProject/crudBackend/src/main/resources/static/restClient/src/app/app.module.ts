import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HttpModule } from '@angular/http';
import { FormsModule } from '@angular/forms';
import { AppComponent } from './app.component';
import { UserFormComponent } from './components/user-form/user-form.component';
import { UserService } from './shared_service/user.service';
import { NavComponent } from './components/nav/nav.component';
import { ForgotPasswordComponent } from './components/forgot-password/forgot-password.component';
import { RegistrationComponent } from './components/registration/registration.component';
import { UserHomeComponent } from './components/user-home/user-home.component';
import { AuthGuard } from './auth.guard';
import { AuthService } from './auth.service';
import { AppRoutingModule } from './app-routing.module';
import { ReactiveFormsModule } from '@angular/forms';
import { UserDataComponent } from './components/user-data/user-data.component';
import { HomeGuestComponent } from './components/home-guest/home-guest.component';
import { UserFolderComponent } from './components/user-folder/user-folder.component';
import { PublicFolderComponent } from './components/public-folder/public-folder.component';
import { HttpClientModule } from '@angular/common/http';
import { BpmnPrivateComponent } from './components/bpmn-private/bpmn-private.component';
import { BpmnPublicComponent } from './components/bpmn-public/bpmn-public.component';

const appRoutes: Routes = [
  { path: '', component: HomeGuestComponent },
  { path: 'uf', component: UserFormComponent },
  { path: 'reg', component: RegistrationComponent },
  { path: 'fp', component: ForgotPasswordComponent },
  { path: 'dir', component: UserFolderComponent },
  { path: 'pubf', component: PublicFolderComponent },
  { path: 'hp', component: UserHomeComponent, canActivate: [AuthGuard] },
  { path: 'ud', component: UserDataComponent, canActivate: [AuthGuard] }
];

@NgModule({
  declarations: [
    AppComponent,
    UserFormComponent,
    NavComponent,
    ForgotPasswordComponent,
    RegistrationComponent,
    UserHomeComponent,
    UserDataComponent,
    HomeGuestComponent,
    UserFolderComponent,
    PublicFolderComponent,
    BpmnPrivateComponent,
    BpmnPublicComponent,
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    AppRoutingModule,
    HttpModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule.forRoot([
      {
        path: '',
        component: HomeGuestComponent,
      },
      {
        path: 'uf',
        component: UserFormComponent,
      },
      {
        path: 'hp',
        component: UserHomeComponent,
        canActivate: [AuthGuard],
      },
      {
        path: 'fp',
        component: ForgotPasswordComponent,
      },
      {
        path: 'reg',
        component: RegistrationComponent,
      },
      {
        path: 'dir/:fName',
        component: UserFolderComponent,
        canActivate: [AuthGuard],
      },
      {
        path: 'ud',
        component: UserDataComponent,
        canActivate: [AuthGuard],
      },
      {
        path: 'pubf/:fName',
        component: PublicFolderComponent,
      },
      {
        path: 'bpmnpr/:mName',
        component: BpmnPrivateComponent,
        canActivate: [AuthGuard],
      },
      {
        path: 'bpmnpub/:mName',
        component: BpmnPublicComponent,
      }
    ])
  ],
  providers: [UserService, AuthService, AuthGuard],
  bootstrap: [AppComponent]
})
export class AppModule { }
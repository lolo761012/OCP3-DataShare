import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { UploadComponent } from './pages/upload/upload.component';
import { DownloadComponent } from './pages/download/download.component';
import { MySpaceComponent } from './pages/myspace/myspace.component';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'register',
    component: RegisterComponent
  },
  {
    path: 'upload',
    component: UploadComponent
  },
  {
    path: 'myspace',
    component: MySpaceComponent
  },
  {
    path: 'downloads/:token',
    component: DownloadComponent
  },
  {
    path: '**',
    redirectTo: 'login'
  }
];
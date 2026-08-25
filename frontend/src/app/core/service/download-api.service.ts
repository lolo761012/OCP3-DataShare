import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { DownloadInfo } from '../models/download-info.model';

@Injectable({
  providedIn: 'root'
})
export class DownloadApiService {

  constructor(private http: HttpClient) {}

  getDownloadInfo(downloadToken: string): Observable<DownloadInfo> {
    return this.http.get<DownloadInfo>(
      '/api/downloads/' + downloadToken
    );
  }

  downloadFile(token: string, password: string | null): Observable<Blob> {
    return this.http.post (
      '/api/downloads/' + token + '/file', {password: password},{responseType: 'blob'}
    );
  }

}
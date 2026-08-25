import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { StoredFileUploadResponse } from '../models/stored-file-upload-response.model';
import { StoredFileList } from '../models/stored-file-list.model';

@Injectable({
  providedIn: 'root'
})
export class FileApiService {

  constructor(private http: HttpClient) {}

  uploadFile(
    file: File,
    expirationDays: number,
    password: string | null
  ): Observable<StoredFileUploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('expirationDays', String(expirationDays));

    if (password) {
      formData.append('password', password);
    }

    return this.http.post<StoredFileUploadResponse>('/api/files', formData);
  }

  getFiles(): Observable<StoredFileList[]> {
    return this.http.get<StoredFileList[]>('/api/files');
  }

  deleteFile(storedFileId: number): Observable<void> {
   return this.http.delete<void> (
      '/api/files/' + storedFileId
    );
  }
}
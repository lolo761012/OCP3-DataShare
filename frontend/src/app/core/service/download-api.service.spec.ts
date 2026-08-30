import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';

import { DownloadApiService } from './download-api.service';
import { DownloadInfo } from '../models/download-info.model';

describe('DownloadApiService', () => {
  let service: DownloadApiService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        DownloadApiService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(DownloadApiService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

    it('gets download information', () => {
     const info = {
        fileName: 'test.pdf',
        size: 1024,
        passwordProtected: false
     } as DownloadInfo;

        service.getDownloadInfo('token').subscribe(result => {
         expect(result).toEqual(info);
        });

        const request =
            httpTesting.expectOne('/api/downloads/token');

        expect(request.request.method).toBe('GET');

        request.flush(info);
    });


    it('downloads a file with a password', () => {
        const blob = new Blob(['contenu test'], {
        type: 'text/plain'
        });

        service.downloadFile('token', 'azerty').subscribe(result => {
            expect(result).toEqual(blob);
        });

        const request =
            httpTesting.expectOne('/api/downloads/token/file');

        expect(request.request.method).toBe('POST');
        expect(request.request.body).toEqual({
            password: 'azerty'
         });
        expect(request.request.responseType).toBe('blob');

        request.flush(blob);
    });
});
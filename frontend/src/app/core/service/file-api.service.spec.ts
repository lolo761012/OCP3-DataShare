import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';

import { FileApiService } from './file-api.service';
import { StoredFileUploadResponse } from '../models/stored-file-upload-response.model';

describe('FileApiService', () => {
  let service: FileApiService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        FileApiService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(FileApiService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  function makeFile(name = 'test.txt', content = 'hello'): File {
    return new File([content], name, { type: 'text/plain' });
  }

  it('sends POST to /api/files with the file as FormData', () => {
    const file = makeFile();

    service.uploadFile(file, 7, null).subscribe();

    const req = httpTesting.expectOne('/api/files');

    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBe(true);
    expect((req.request.body as FormData).get('file')).toBe(file);

    req.flush({
      id: 1,
      fileName: 'test.txt',
      size: 5,
      downloadToken: 'token-123',
      expiresAt: '2026-08-30T12:00:00'
    } as StoredFileUploadResponse);
  });

  it('includes expirationDays as a string field in the FormData', () => {
    const file = makeFile();

    service.uploadFile(file, 3, null).subscribe();

    const req = httpTesting.expectOne('/api/files');

    expect((req.request.body as FormData).get('expirationDays')).toBe('3');

    req.flush({} as StoredFileUploadResponse);
  });

  it('omits the password field when no password is provided', () => {
    const file = makeFile();

    service.uploadFile(file, 7, null).subscribe();

    const req = httpTesting.expectOne('/api/files');

    expect((req.request.body as FormData).get('password')).toBeNull();

    req.flush({} as StoredFileUploadResponse);
  });

  it('includes the password field when a password is provided', () => {
    const file = makeFile();

    service.uploadFile(file, 7, 'secret1').subscribe();

    const req = httpTesting.expectOne('/api/files');

    expect((req.request.body as FormData).get('password')).toBe('secret1');

    req.flush({} as StoredFileUploadResponse);
  });

  it('does not manually set a Content-Type header', () => {
    const file = makeFile();

    service.uploadFile(file, 7, null).subscribe();

    const req = httpTesting.expectOne('/api/files');

    expect(req.request.headers.has('Content-Type')).toBe(false);

    req.flush({} as StoredFileUploadResponse);
  });

  it('returns the backend response on success', () => {
    const file = makeFile();
    const response: StoredFileUploadResponse = {
      id: 42,
      fileName: 'test.txt',
      size: 5,
      downloadToken: 'token-abc',
      expiresAt: '2026-08-30T12:00:00'
    };

    let received: StoredFileUploadResponse | undefined;

    service.uploadFile(file, 7, null).subscribe((value) => {
      received = value;
    });

    httpTesting.expectOne('/api/files').flush(response);

    expect(received).toEqual(response);
  });

  it('propagates an HTTP error (e.g. 413)', () => {
    const file = makeFile();

    let status: number | undefined;

    service.uploadFile(file, 7, null).subscribe({
      error: (error) => {
        status = error.status;
      }
    });

    httpTesting.expectOne('/api/files').flush(
      { message: 'File too large' },
      { status: 413, statusText: 'Payload Too Large' }
    );

    expect(status).toBe(413);
  });
});
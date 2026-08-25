import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, Subject, throwError } from 'rxjs';
import { vi } from 'vitest';

import { UploadComponent } from './upload.component';
import { FileApiService } from '../../core/service/file-api.service';
import { StoredFileUploadResponse } from '../../core/models/stored-file-upload-response.model';
import { AuthService } from '../../core/service/auth.service';
import { provideRouter } from '@angular/router';

describe('UploadComponent', () => {
  let fixture: ComponentFixture<UploadComponent>;
  let component: UploadComponent;
  let uploadFileMock: ReturnType<typeof vi.fn>;
  let logoutMock: ReturnType<typeof vi.fn>;


  function makeFile(name: string, sizeBytes: number, type = 'text/plain'): File {
    const file = new File([new Uint8Array(Math.min(sizeBytes, 1024))], name, { type });
    Object.defineProperty(file, 'size', { value: sizeBytes });
    return file;
  }

  function selectFile(file: File): void {
    const input = document.createElement('input');
    Object.defineProperty(input, 'files', { value: [file] });
    component.onFileSelected({ target: input } as unknown as Event);
  }

  beforeEach(async () => {
    uploadFileMock = vi.fn();
    logoutMock = vi.fn();

    Object.defineProperty(navigator, 'clipboard', {
      value: { writeText: vi.fn().mockResolvedValue(undefined) },
      configurable: true
    });

    await TestBed.configureTestingModule({
      imports: [UploadComponent],
      providers: [
  provideRouter([]),
  {
    provide: FileApiService,
    useValue: { uploadFile: uploadFileMock }
  },
  {
  provide: AuthService,
  useValue: {
    logout: logoutMock,
    isAuthenticated: vi.fn().mockReturnValue(true)
  }
}
]
    }).compileComponents();

    fixture = TestBed.createComponent(UploadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('creates the component', () => {
    expect(component).toBeTruthy();
  });

  it('has no file selected by default', () => {
    expect(component.selectedFile()).toBeNull();
  });

  it('stores the selected file', () => {
    const file = makeFile('report.pdf', 1024);

    selectFile(file);

    expect(component.selectedFile()).toBe(file);
  });

  it('flags a file larger than 1 GB as too large', () => {
    const file = makeFile('big.zip', 1024 * 1024 * 1024 + 1);

    selectFile(file);

    expect(component.fileTooLarge()).toBe(true);
  });

  it('does not flag a file at exactly the 1 GB limit as too large', () => {
    const file = makeFile('exact.zip', 1024 * 1024 * 1024);

    selectFile(file);

    expect(component.fileTooLarge()).toBe(false);
  });

  it('flags a forbidden extension', () => {
    const file = makeFile('virus.exe', 1024);

    selectFile(file);

    expect(component.fileExtensionForbidden()).toBe(true);
  });

  it('is case-insensitive on forbidden extensions', () => {
    const file = makeFile('virus.EXE', 1024);

    selectFile(file);

    expect(component.fileExtensionForbidden()).toBe(true);
  });

  it('does not flag an allowed extension', () => {
    const file = makeFile('report.pdf', 1024);

    selectFile(file);

    expect(component.fileExtensionForbidden()).toBe(false);
  });

  it('does not call uploadFile when submitting without a file', () => {
    component.onSubmit();

    expect(uploadFileMock).not.toHaveBeenCalled();
  });

  it('does not call uploadFile when the file is too large', () => {
    const file = makeFile('big.zip', 1024 * 1024 * 1024 + 1);
    selectFile(file);

    component.onSubmit();

    expect(uploadFileMock).not.toHaveBeenCalled();
  });

  it('does not call uploadFile when the extension is forbidden', () => {
    const file = makeFile('virus.exe', 1024);
    selectFile(file);

    component.onSubmit();

    expect(uploadFileMock).not.toHaveBeenCalled();
  });

  it('does not call uploadFile when the password is shorter than 6 characters', () => {
    const file = makeFile('report.pdf', 1024);
    selectFile(file);
    component.optionsForm.get('password')?.setValue('abc');

    component.onSubmit();

    expect(uploadFileMock).not.toHaveBeenCalled();
  });

  it('calls uploadFile with the selected file, expirationDays and password', () => {
    uploadFileMock.mockReturnValue(new Subject());
    const file = makeFile('report.pdf', 1024);
    selectFile(file);
    component.optionsForm.get('expirationDays')?.setValue(3);
    component.optionsForm.get('password')?.setValue('secret1');

    component.onSubmit();

    expect(uploadFileMock).toHaveBeenCalledWith(file, 3, 'secret1');
  });

  it('sends null as password when the field is empty', () => {
    uploadFileMock.mockReturnValue(new Subject());
    const file = makeFile('report.pdf', 1024);
    selectFile(file);

    component.onSubmit();

    expect(uploadFileMock).toHaveBeenCalledWith(file, 7, null);
  });

  it('sets loading to true while the request is pending', () => {
    uploadFileMock.mockReturnValue(new Subject());
    const file = makeFile('report.pdf', 1024);
    selectFile(file);

    component.onSubmit();

    expect(component.uploading()).toBe(true);
  });

  it('stores the response and clears loading on success', () => {
    const response: StoredFileUploadResponse = {
      id: 1,
      fileName: 'report.pdf',
      size: 1024,
      downloadToken: 'abc-123',
      expiresAt: '2026-08-30T12:00:00'
    };
    uploadFileMock.mockReturnValue(of(response));
    const file = makeFile('report.pdf', 1024);
    selectFile(file);

    component.onSubmit();

    expect(component.uploading()).toBe(false);
    expect(component.uploadResult()).toEqual(response);
  });

  it('builds a download link from the returned token', () => {
    const response: StoredFileUploadResponse = {
      id: 1,
      fileName: 'report.pdf',
      size: 1024,
      downloadToken: 'abc-123',
      expiresAt: '2026-08-30T12:00:00'
    };
    uploadFileMock.mockReturnValue(of(response));
    const file = makeFile('report.pdf', 1024);
    selectFile(file);

    component.onSubmit();

    expect(component.downloadLink()).toContain('abc-123');
  });


  it('uses a fallback message for a 413 error without a backend message', () => {
    uploadFileMock.mockReturnValue(
      throwError(() => ({ status: 413, error: {} }))
    );
    const file = makeFile('report.pdf', 1024);
    selectFile(file);

    component.onSubmit();

    expect(component.errorMessage()).toContain('1 Go');
  });

  it('copies the download link to the clipboard', async () => {
    const response: StoredFileUploadResponse = {
      id: 1,
      fileName: 'report.pdf',
      size: 1024,
      downloadToken: 'abc-123',
      expiresAt: '2026-08-30T12:00:00'
    };
    uploadFileMock.mockReturnValue(of(response));
    const file = makeFile('report.pdf', 1024);
    selectFile(file);
    component.onSubmit();

    await component.copyLink();

    expect(navigator.clipboard.writeText).toHaveBeenCalledWith(component.downloadLink());
  });

  it('displays a specific message when the backend is unreachable (status 0)', () => {
  uploadFileMock.mockReturnValue(
    throwError(() => ({ status: 0, error: null }))
  );
  const file = makeFile('report.pdf', 1024);
  selectFile(file);

  component.onSubmit();

  expect(component.errorMessage()).toBe('Impossible de contacter le serveur backend.');
  });

  it('logs out and displays a session message on 401, without retrying automatically', () => {
  uploadFileMock.mockReturnValue(
    throwError(() => ({ status: 401, error: { message: 'Invalid or expired JWT' } }))
  );
  const file = makeFile('report.pdf', 1024);
  selectFile(file);

  component.onSubmit();

  expect(logoutMock).toHaveBeenCalledTimes(1);
  expect(component.errorMessage()).toBe(
    'Votre session était invalide et a été réinitialisée. Vous pouvez réessayer votre envoi.'
  );
  expect(uploadFileMock).toHaveBeenCalledTimes(1);
  });

});
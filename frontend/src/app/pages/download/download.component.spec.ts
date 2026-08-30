import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, Subject, throwError } from 'rxjs';
import { vi } from 'vitest';

import { DownloadComponent } from './download.component';
import { DownloadApiService } from '../../core/service/download-api.service';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../../core/service/auth.service';
import { DownloadInfo } from '../../core/models/download-info.model';

describe('DownloadComponent', () => {
  let fixture: ComponentFixture<DownloadComponent>;
  let component: DownloadComponent;
  let getDownloadInfoMock: ReturnType<typeof vi.fn>;
  let downloadFileMock: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    getDownloadInfoMock = vi.fn().mockReturnValue(of(null));
    downloadFileMock = vi.fn();

    await TestBed.configureTestingModule({
      imports: [DownloadComponent],
      providers: [
        {
          provide: DownloadApiService,
          useValue: {
            getDownloadInfo: getDownloadInfoMock,
            downloadFile: downloadFileMock
          }
        },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: vi.fn().mockReturnValue('token')
              }
            }
          }
        },
        {
          provide: AuthService,
          useValue: {
            isAuthenticated: vi.fn().mockReturnValue(false)
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DownloadComponent);
    component = fixture.componentInstance;
  });

  it('creates the component', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('loads download information', () => {
    const info = {
        fileName: 'test.pdf',
        size: 1024,
        passwordProtected: false
    };

    getDownloadInfoMock.mockReturnValue(of(info));

    component.ngOnInit();

    expect(getDownloadInfoMock).toHaveBeenCalledWith('token');
    expect(component.downloadInfo()).toEqual(info);
    expect(component.loading()).toBe(false);
  });

  it('displays an expired message for a 410 response', () => {
    getDownloadInfoMock.mockReturnValue(
        throwError(() => ({ status: 410 }))
    );

    component.ngOnInit();

    expect(component.errorMessage()).toBe('Ce fichier a expiré.');
    expect(component.loading()).toBe(false);
  });

  it('downloads the file with the entered password', () => {
    const info = {
     fileName: 'test.pdf',
        size: 1024,
        passwordProtected: true
    } as DownloadInfo;

    getDownloadInfoMock.mockReturnValue(of(info));
    downloadFileMock.mockReturnValue(new Subject<Blob>());

    component.ngOnInit();

    component.downloadForm.get('password')?.setValue('azerty');

    component.onDownload();

    expect(downloadFileMock).toHaveBeenCalledWith('token', 'azerty');
    expect(component.downloading()).toBe(true);
  });

  it('displays an error for a wrong password', () => {
    const info = {
        fileName: 'report.pdf',
        size: 1024,
        passwordProtected: true
     } as DownloadInfo;

    getDownloadInfoMock.mockReturnValue(of(info));

    downloadFileMock.mockReturnValue(
        throwError(() => ({ status: 403 }))
    );

    component.ngOnInit();
    component.downloadForm.get('password')?.setValue('wrong-password');

    component.onDownload();

    expect(component.errorMessage()).toBe('Mot de passe incorrect.');
    expect(component.downloading()).toBe(false);
  });

  it('renders file information and password field', () => {
    const info = {
        fileName: 'test.pdf',
        size: 1024,
        passwordProtected: true
    } as DownloadInfo;

    getDownloadInfoMock.mockReturnValue(of(info));

    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;

    expect(element.textContent).toContain('test.pdf');
    expect(element.textContent).toContain('1,0 Ko');
    expect(element.querySelector('#password')).not.toBeNull();
    expect(element.textContent).toContain('Télécharger');
   });
});
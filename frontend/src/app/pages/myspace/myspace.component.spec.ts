import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { MySpaceComponent } from './myspace.component';
import { FileApiService } from '../../core/service/file-api.service';
import { AuthService } from '../../core/service/auth.service';

import { StoredFileList } from '../../core/models/stored-file-list.model';

import { provideRouter, Router } from '@angular/router';

describe('MySpaceComponent', () => {
  let fixture: ComponentFixture<MySpaceComponent>;
  let component: MySpaceComponent;
  let getFilesMock: ReturnType<typeof vi.fn>;
  let deleteFileMock: ReturnType<typeof vi.fn>;
  let logoutMock: ReturnType<typeof vi.fn>;
  let router: Router;

  beforeEach(async () => {
    getFilesMock = vi.fn().mockReturnValue(of([]));
    deleteFileMock = vi.fn();
    logoutMock = vi.fn();

    await TestBed.configureTestingModule({
      imports: [MySpaceComponent],
      providers: [
        provideRouter([]),
        {
          provide: FileApiService,
          useValue: {
            getFiles: getFilesMock,
            deleteFile: deleteFileMock
          }
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

    fixture = TestBed.createComponent(MySpaceComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('creates the component', () => {
    expect(component).toBeTruthy();
  });

  it('loads files', () => {
    const files = [
        {
            id: 1,
            fileName: 'test.pdf',
            size: 1024,
            status: 'VALID',
            downloadToken: 'azerty'
        }
    ] as StoredFileList[];

    getFilesMock.mockReturnValue(of(files));

    component.loadFiles();

    expect(component.files()).toEqual(files);
    expect(component.loading()).toBe(false);
   });

    it('filters files by status', () => {
        const files = [
        {
            id: 1,
            fileName: 'active.txt',
            status: 'VALID'
        },
        {
            id: null,
            fileName: 'expired.txt',
            status: 'EXPIRED'
        }
        ] as StoredFileList[];

        component.files.set(files);

        component.setFilter('VALID');

        expect(component.filteredFiles()).toHaveLength(1);
        expect(component.filteredFiles()[0].fileName).toBe('active.txt');

        component.setFilter('EXPIRED');

        expect(component.filteredFiles()).toHaveLength(1);
        expect(component.filteredFiles()[0].fileName).toBe('expired.txt');
    });

    it('logs out and redirects to login on 401', () => {
        getFilesMock.mockReturnValue(
            throwError(() => ({ status: 401 }))
        );

        const navigateSpy = vi
        .spyOn(router, 'navigate')
        .mockResolvedValue(true);

        component.loadFiles();

        expect(logoutMock).toHaveBeenCalledTimes(1);
        expect(navigateSpy).toHaveBeenCalledWith(['/login']);
    });


    it('deletes a confirmed file and reloads files', () => {
        const file = {
        id: 1,
        fileName: 'test.pdf',
        status: 'VALID'
        } as StoredFileList;

    vi.spyOn(window, 'confirm').mockReturnValue(true);
    deleteFileMock.mockReturnValue(of(void 0));

    const loadFilesSpy = vi.spyOn(component, 'loadFiles');

    component.deleteFile(file);

    expect(deleteFileMock).toHaveBeenCalledWith(1);
    expect(component.successMessage()).toBe('Fichier supprimé avec succès.');
    expect(loadFilesSpy).toHaveBeenCalledTimes(1);
    });

    it('renders active and expired files', () => {
        const files = [
        {
            id: 1,
            fileName: 'active.txt',
            size: 1024,
            uploadedAt: '2026-08-30T10:00:00',
            expiresAt: new Date(Date.now() + 2 * 24 * 60 * 60 * 1000).toISOString().slice(0, 19),
            status: 'VALID',
            downloadToken: 'active-token'
        },
        {
            id: null,
            fileName: 'expired.txt',
            size: 2048,
            uploadedAt: '2026-08-20T10:00:00',
            expiresAt: '2026-08-21T10:00:00',
            status: 'EXPIRED',
            downloadToken: 'expired-token'
        }
        ] as StoredFileList[];

        component.files.set(files);

        fixture.detectChanges();

        const element = fixture.nativeElement as HTMLElement;

        expect(element.textContent).toContain('active.txt');
        expect(element.textContent).toContain('expired.txt');
    });
});
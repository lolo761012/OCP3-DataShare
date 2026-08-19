import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of, Subject, throwError } from 'rxjs';
import { vi } from 'vitest';

import { LoginComponent } from './login.component';
import { AuthApiService } from '../../core/service/auth-api.service';
import { AuthService } from '../../core/service/auth.service';
import { LoginResponse } from '../../core/models/login-response.model';

describe('LoginComponent', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let component: LoginComponent;
  let router: Router;
  let loginMock: ReturnType<typeof vi.fn>;
  let saveTokenMock: ReturnType<typeof vi.fn>;

  const validEmail = 'user@example.com';
  const validPassword = 'password123';

  beforeEach(async () => {
    loginMock = vi.fn();
    saveTokenMock = vi.fn();

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        provideRouter([]),
        {
          provide: AuthApiService,
          useValue: {
            login: loginMock
          }
        },
        {
          provide: AuthService,
          useValue: {
            saveToken: saveTokenMock
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  function fillValidForm(): void {
    component.loginForm.setValue({
      email: validEmail,
      password: validPassword
    });
  }

  it('creates the component', () => {
    expect(component).toBeTruthy();
  });

  it('has an invalid form by default', () => {
    expect(component.loginForm.invalid).toBe(true);
  });

  it('requires an email', () => {
    const email = component.loginForm.get('email');

    email?.setValue('');

    expect(email?.hasError('required')).toBe(true);
  });

  it('rejects an invalid email format', () => {
    const email = component.loginForm.get('email');

    email?.setValue('not-an-email');

    expect(email?.hasError('email')).toBe(true);
  });

  it('requires a password', () => {
    const password = component.loginForm.get('password');

    password?.setValue('');

    expect(password?.hasError('required')).toBe(true);
  });

  it('does not call login when the form is invalid', () => {
    component.onSubmit();

    expect(loginMock).not.toHaveBeenCalled();
  });

  it('calls login with email and password', () => {
    loginMock.mockReturnValue(of<LoginResponse>({ token: 'jwt-token' }));
    fillValidForm();

    component.onSubmit();

    expect(loginMock).toHaveBeenCalledTimes(1);
    expect(loginMock).toHaveBeenCalledWith({
      email: validEmail,
      password: validPassword
    });
  });

  it('sets loading to true and disables submit while request is pending', () => {
    const pendingResponse = new Subject<LoginResponse>();
    loginMock.mockReturnValue(pendingResponse.asObservable());
    fillValidForm();

    component.onSubmit();
    fixture.detectChanges();

    const button = fixture.nativeElement.querySelector(
      'button[type="submit"]'
    ) as HTMLButtonElement;

    expect(component.loading()).toBe(true);
    expect(button.disabled).toBe(true);

    pendingResponse.complete();
  });

  it('stores the returned token after successful login', () => {
    loginMock.mockReturnValue(of<LoginResponse>({ token: 'jwt-token' }));
    fillValidForm();

    component.onSubmit();

    expect(saveTokenMock).toHaveBeenCalledTimes(1);
    expect(saveTokenMock).toHaveBeenCalledWith('jwt-token');
  });

  it('sets success and clears loading after successful login', () => {
    loginMock.mockReturnValue(of<LoginResponse>({ token: 'jwt-token' }));
    fillValidForm();

    component.onSubmit();

    expect(component.loading()).toBe(false);
    expect(component.success()).toBe(true);
    expect(component.errorMessage()).toBe('');
  });

  it('does not reset the form after successful login', () => {
    loginMock.mockReturnValue(of<LoginResponse>({ token: 'jwt-token' }));
    fillValidForm();

    component.onSubmit();

    expect(component.loginForm.get('email')?.value).toBe(validEmail);
    expect(component.loginForm.get('password')?.value).toBe(validPassword);
  });

  it('does not navigate automatically after successful login', () => {
    const navigateSpy = vi.spyOn(router, 'navigate');
    const navigateByUrlSpy = vi.spyOn(router, 'navigateByUrl');

    loginMock.mockReturnValue(of<LoginResponse>({ token: 'jwt-token' }));
    fillValidForm();

    component.onSubmit();

    expect(navigateSpy).not.toHaveBeenCalled();
    expect(navigateByUrlSpy).not.toHaveBeenCalled();
  });

  it('displays a 401 backend message and does not store a token', () => {
    loginMock.mockReturnValue(
      throwError(() => ({
        status: 401,
        error: { message: 'Bad credentials' }
      }))
    );
    fillValidForm();

    component.onSubmit();

    expect(component.loading()).toBe(false);
    expect(component.success()).toBe(false);
    expect(component.errorMessage()).toBe('Bad credentials');
    expect(saveTokenMock).not.toHaveBeenCalled();
  });

  it('uses the fallback message when backend message is unavailable', () => {
    loginMock.mockReturnValue(
      throwError(() => ({ status: 500 }))
    );
    fillValidForm();

    component.onSubmit();

    expect(component.loading()).toBe(false);
    expect(component.errorMessage()).toBe('Serveur indisponible.');
    expect(saveTokenMock).not.toHaveBeenCalled();
  });
});
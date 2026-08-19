import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { provideRouter } from '@angular/router';
import { of, Subject, throwError } from 'rxjs';
import { vi } from 'vitest';

import { RegisterComponent } from './register.component';
import { AuthApiService } from '../../core/service/auth-api.service';
import { RegisterResponse } from '../../core/models/register-response.model';

describe('RegisterComponent', () => {
  let fixture: ComponentFixture<RegisterComponent>;
  let component: RegisterComponent;
  let router: Router;
  let registerMock: ReturnType<typeof vi.fn>;

  const validEmail = 'user@example.com';
  const validPassword = 'password123';

  beforeEach(async () => {
    registerMock = vi.fn();

    await TestBed.configureTestingModule({
      imports: [RegisterComponent],
      providers: [
        provideRouter([]),
        {
          provide: AuthApiService,
          useValue: {
            register: registerMock
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  function fillValidForm(): void {
    component.registerForm.setValue({
      email: validEmail,
      password: validPassword,
      confirmPassword: validPassword
    });
  }

  it('creates the component', () => {
    expect(component).toBeTruthy();
  });

  it('has an invalid form by default', () => {
    expect(component.registerForm.invalid).toBe(true);
  });

  it('requires an email', () => {
    const email = component.registerForm.get('email');

    email?.setValue('');

    expect(email?.hasError('required')).toBe(true);
  });

  it('rejects an invalid email format', () => {
    const email = component.registerForm.get('email');

    email?.setValue('not-an-email');

    expect(email?.hasError('email')).toBe(true);
  });

  it('requires a password', () => {
    const password = component.registerForm.get('password');

    password?.setValue('');

    expect(password?.hasError('required')).toBe(true);
  });

  it('rejects a password shorter than 8 characters', () => {
    const password = component.registerForm.get('password');

    password?.setValue('short');

    expect(password?.hasError('minlength')).toBe(true);
  });

  it('requires password confirmation', () => {
    const confirmPassword = component.registerForm.get('confirmPassword');

    confirmPassword?.setValue('');

    expect(confirmPassword?.hasError('required')).toBe(true);
  });

  it('sets passwordsMismatch when confirmation differs', () => {
    component.registerForm.setValue({
      email: validEmail,
      password: validPassword,
      confirmPassword: 'different-password'
    });

    expect(component.registerForm.hasError('passwordsMismatch')).toBe(true);
  });

  it('accepts a valid form', () => {
    fillValidForm();

    expect(component.registerForm.valid).toBe(true);
  });

  it('does not call register when the form is invalid', () => {
    component.onSubmit();

    expect(registerMock).not.toHaveBeenCalled();
  });

  it('calls register with email and password only', () => {
    registerMock.mockReturnValue(
      of<RegisterResponse>({ id: 1, email: validEmail })
    );
    fillValidForm();

    component.onSubmit();

    expect(registerMock).toHaveBeenCalledTimes(1);
    expect(registerMock).toHaveBeenCalledWith({
      email: validEmail,
      password: validPassword
    });
  });

  it('sets loading to true and disables submit while request is pending', () => {
    const pendingResponse = new Subject<RegisterResponse>();
    registerMock.mockReturnValue(pendingResponse.asObservable());
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

  it('handles successful registration', () => {
    registerMock.mockReturnValue(
      of<RegisterResponse>({ id: 1, email: validEmail })
    );
    fillValidForm();

    component.onSubmit();

    expect(component.loading()).toBe(false);
    expect(component.success()).toBe(true);
    expect(component.errorMessage()).toBe('');
    expect(component.registerForm.get('email')?.value).toBeNull();
    expect(component.registerForm.get('password')?.value).toBeNull();
    expect(component.registerForm.get('confirmPassword')?.value).toBeNull();
    expect(component.submitted).toBe(false);
  });

  it('displays the backend error message', () => {
    registerMock.mockReturnValue(
      throwError(() => ({
        status: 409,
        error: { message: 'Email is already used' }
      }))
    );
    fillValidForm();

    component.onSubmit();

    expect(component.loading()).toBe(false);
    expect(component.success()).toBe(false);
    expect(component.errorMessage()).toBe('Email is already used');
  });

  it('uses the fallback message when backend message is unavailable', () => {
    registerMock.mockReturnValue(
      throwError(() => ({ status: 500 }))
    );
    fillValidForm();

    component.onSubmit();

    expect(component.loading()).toBe(false);
    expect(component.errorMessage()).toBe('Serveur indisponible.');
  });

  it('does not navigate automatically after successful registration', () => {
    const navigateSpy = vi.spyOn(router, 'navigate');
    const navigateByUrlSpy = vi.spyOn(router, 'navigateByUrl');

    registerMock.mockReturnValue(
      of<RegisterResponse>({ id: 1, email: validEmail })
    );
    fillValidForm();

    component.onSubmit();

    expect(navigateSpy).not.toHaveBeenCalled();
    expect(navigateByUrlSpy).not.toHaveBeenCalled();
  });
});
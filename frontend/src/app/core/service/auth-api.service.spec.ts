import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';

import { AuthApiService } from './auth-api.service';
import { RegisterRequest } from '../models/register-request.model';
import { RegisterResponse } from '../models/register-response.model';
import { LoginRequest } from '../models/login-request.model';
import { LoginResponse } from '../models/login-response.model';

describe('AuthApiService', () => {
  let service: AuthApiService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthApiService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(AuthApiService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('register sends POST to /api/auth/register with the expected body', () => {
    const request: RegisterRequest = {
      email: 'user@example.com',
      password: 'password123'
    };

    service.register(request).subscribe();

    const httpRequest = httpTesting.expectOne('/api/auth/register');

    expect(httpRequest.request.method).toBe('POST');
    expect(httpRequest.request.body).toEqual(request);

    httpRequest.flush({ id: 1, email: request.email });
  });

  it('register returns the backend response', () => {
    const request: RegisterRequest = {
      email: 'user@example.com',
      password: 'password123'
    };
    const response: RegisterResponse = {
      id: 1,
      email: request.email
    };

    let received: RegisterResponse | undefined;

    service.register(request).subscribe((value) => {
      received = value;
    });

    httpTesting.expectOne('/api/auth/register').flush(response);

    expect(received).toEqual(response);
  });

  it('register propagates an HTTP 409 error', () => {
    const request: RegisterRequest = {
      email: 'user@example.com',
      password: 'password123'
    };

    let status: number | undefined;

    service.register(request).subscribe({
      error: (error) => {
        status = error.status;
      }
    });

    httpTesting.expectOne('/api/auth/register').flush(
      { message: 'Email is already used' },
      { status: 409, statusText: 'Conflict' }
    );

    expect(status).toBe(409);
  });

  it('login sends POST to /api/auth/login with the expected body', () => {
    const request: LoginRequest = {
      email: 'user@example.com',
      password: 'password123'
    };

    service.login(request).subscribe();

    const httpRequest = httpTesting.expectOne('/api/auth/login');

    expect(httpRequest.request.method).toBe('POST');
    expect(httpRequest.request.body).toEqual(request);

    httpRequest.flush({ token: 'jwt-token' });
  });

  it('login returns the backend response', () => {
    const request: LoginRequest = {
      email: 'user@example.com',
      password: 'password123'
    };
    const response: LoginResponse = {
      token: 'jwt-token'
    };

    let received: LoginResponse | undefined;

    service.login(request).subscribe((value) => {
      received = value;
    });

    httpTesting.expectOne('/api/auth/login').flush(response);

    expect(received).toEqual(response);
  });

  it('login propagates an HTTP 401 error', () => {
    const request: LoginRequest = {
      email: 'user@example.com',
      password: 'wrong-password'
    };

    let status: number | undefined;

    service.login(request).subscribe({
      error: (error) => {
        status = error.status;
      }
    });

    httpTesting.expectOne('/api/auth/login').flush(
      { message: 'Bad credentials' },
      { status: 401, statusText: 'Unauthorized' }
    );

    expect(status).toBe(401);
  });
});
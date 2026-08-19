import { TestBed } from '@angular/core/testing';
import {
  HttpClient,
  provideHttpClient,
  withInterceptors
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';

import { authInterceptor } from './auth.interceptor';
import { AuthService } from '../service/auth.service';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpTesting: HttpTestingController;
  let authService: AuthService;

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting()
      ]
    });

    http = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService);
  });

  afterEach(() => {
    httpTesting.verify();
    localStorage.clear();
  });

  it('does not add Authorization to /api/files when no token exists', () => {
    http.get('/api/files').subscribe();

    const req = httpTesting.expectOne('/api/files');

    expect(req.request.headers.has('Authorization')).toBe(false);

    req.flush([]);
  });

  it('adds Bearer token to /api/files when a token exists', () => {
    authService.saveToken('jwt-token');

    http.get('/api/files').subscribe();

    const req = httpTesting.expectOne('/api/files');

    expect(req.request.headers.get('Authorization')).toBe('Bearer jwt-token');

    req.flush([]);
  });

  it('does not add Authorization to /api/auth/login', () => {
    authService.saveToken('jwt-token');

    http.post('/api/auth/login', {}).subscribe();

    const req = httpTesting.expectOne('/api/auth/login');

    expect(req.request.headers.has('Authorization')).toBe(false);

    req.flush({ token: 'new-token' });
  });

  it('does not add Authorization to /api/auth/register', () => {
    authService.saveToken('jwt-token');

    http.post('/api/auth/register', {}).subscribe();

    const req = httpTesting.expectOne('/api/auth/register');

    expect(req.request.headers.has('Authorization')).toBe(false);

    req.flush({ id: 1, email: 'user@example.com' });
  });

  it('does not add Authorization to /actuator/health', () => {
    authService.saveToken('jwt-token');

    http.get('/actuator/health').subscribe();

    const req = httpTesting.expectOne('/actuator/health');

    expect(req.request.headers.has('Authorization')).toBe(false);

    req.flush({ status: 'UP' });
  });

  it('does not add Authorization to an external URL', () => {
    authService.saveToken('jwt-token');

    http.get('https://example.com/data').subscribe();

    const req = httpTesting.expectOne('https://example.com/data');

    expect(req.request.headers.has('Authorization')).toBe(false);

    req.flush({});
  });

  it('does not send a stored token to /api/auth/login even if the token is stale', () => {
    authService.saveToken('expired-or-invalid-token');

    http.post('/api/auth/login', {
      email: 'user@example.com',
      password: 'password123'
    }).subscribe();

    const req = httpTesting.expectOne('/api/auth/login');

    expect(req.request.headers.has('Authorization')).toBe(false);

    req.flush({ token: 'fresh-token' });
  });
});
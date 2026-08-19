import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;

  beforeEach(() => {
    localStorage.clear();
    service = new AuthService();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('saveToken stores the token in localStorage', () => {
    service.saveToken('jwt-token');

    expect(localStorage.getItem('token')).toBe('jwt-token');
  });

  it('getToken returns the stored token', () => {
    service.saveToken('jwt-token');

    expect(service.getToken()).toBe('jwt-token');
  });

  it('getToken returns null when no token is stored', () => {
    expect(service.getToken()).toBeNull();
  });

  it('isAuthenticated returns true when a token is stored', () => {
    service.saveToken('jwt-token');

    expect(service.isAuthenticated()).toBe(true);
  });

  it('isAuthenticated returns false when no token is stored', () => {
    expect(service.isAuthenticated()).toBe(false);
  });

  it('logout removes the token from localStorage', () => {
    service.saveToken('jwt-token');

    service.logout();

    expect(service.getToken()).toBeNull();
  });
});
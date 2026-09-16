import { HttpErrorResponse } from '@angular/common/http';
import { apiError } from './auth.interceptor';

describe('apiError', () => {
  it('prefers JSON error field', () => {
    const err = new HttpErrorResponse({
      status: 409,
      error: { error: 'User already active' }
    });
    expect(apiError(err)).toBe('User already active');
  });

  it('falls back to message field then HTTP message', () => {
    expect(
      apiError(new HttpErrorResponse({ status: 400, error: { message: 'bad' }, statusText: 'Bad Request' }))
    ).toBe('bad');
    expect(apiError(new HttpErrorResponse({ status: 500, statusText: 'Server Error', url: '/api/me' }))).toContain(
      '500'
    );
  });

  it('uses string bodies and a generic fallback', () => {
    expect(apiError(new HttpErrorResponse({ status: 401, error: 'nope' }))).toBe('nope');
    expect(apiError('boom')).toBe('Request failed');
  });
});

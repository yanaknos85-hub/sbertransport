import { AxiosError } from 'axios';

import { CustomErrorCode } from '../constants/constants.app';
import { getErrorCode } from './getErrorCode';

describe('getErrorCode', () => {
  it('should return response status for axios error with status', () => {
    const mockAxiosError = {
      isAxiosError: true,
      response: {
        status: 404,
      },
      code: undefined,
      message: 'Not Found',
    } as AxiosError;

    expect(getErrorCode(mockAxiosError)).toBe(404);
  });

  it('should return TIMEOUT for axios timeout error (ECONNABORTED)', () => {
    const mockAxiosError = {
      isAxiosError: true,
      response: undefined,
      code: 'ECONNABORTED',
      message: 'timeout',
    } as AxiosError;

    expect(getErrorCode(mockAxiosError)).toBe(CustomErrorCode.TIMEOUT);
  });

  it('should return TIMEOUT for axios timeout error in message', () => {
    const mockAxiosError = {
      isAxiosError: true,
      response: undefined,
      code: undefined,
      message: 'Request timeout of 5000ms exceeded',
    } as AxiosError;

    expect(getErrorCode(mockAxiosError)).toBe(CustomErrorCode.TIMEOUT);
  });

  it('should return CORS for Network Error in regular Error', () => {
    const error = new Error('Network Error occurred');

    expect(getErrorCode(error)).toBe(CustomErrorCode.CORS);
  });

  it('should return TYPES for Expecting error in regular Error', () => {
    const error = new Error('Expecting something went wrong');

    expect(getErrorCode(error)).toBe(CustomErrorCode.TYPES);
  });

  it('should return UNDEFINED_FIELD for Cannot read error in regular Error', () => {
    const error = new Error('Cannot read property of undefined');

    expect(getErrorCode(error)).toBe(CustomErrorCode.UNDEFINED_FIELD);
  });

  it('should return UNKNOWN for unknown error types', () => {
    const error = new Error('Some random error');

    expect(getErrorCode(error)).toBe(CustomErrorCode.UNKNOWN);
  });

  it('should handle different HTTP status codes', () => {
    const statuses = [200, 400, 401, 403, 500, 502, 503];

    statuses.forEach(status => {
      const mockAxiosError = {
        isAxiosError: true,
        response: { status },
        message: 'Error',
      } as AxiosError;

      expect(getErrorCode(mockAxiosError)).toBe(status);
    });
  });

  it('should handle axios error without response but with other properties', () => {
    const mockAxiosError = {
      isAxiosError: true,
      response: undefined,
      code: 'SOME_CODE',
      message: 'Some message',
    } as AxiosError;

    expect(getErrorCode(mockAxiosError)).toBe(CustomErrorCode.UNKNOWN);
  });
});

import { CustomErrorCode } from 'constants/constants.app';
import { getErrorCode } from '../getErrorCode';

describe('getErrorCode', () => {
  it('should return status if axios error has response status', () => {
    const error = {
      isAxiosError: true,
      response: { status: 404 },
    };

    expect(getErrorCode(error)).toBe(404);
  });

  it('should return CustomErrorCode.TIMEOUT if axios error has ECONNABORTED code', () => {
    const error = {
      isAxiosError: true,
      code: 'ECONNABORTED',
    };

    expect(getErrorCode(error)).toBe(CustomErrorCode.TIMEOUT);
  });

  it('should return CustomErrorCode.TIMEOUT if axios error message includes timeout', () => {
    const error = {
      isAxiosError: true,
      message: 'timeout of 5000ms exceeded',
    };

    expect(getErrorCode(error)).toBe(CustomErrorCode.TIMEOUT);
  });

  it('should return CustomErrorCode.CORS if error message includes Network Error', () => {
    const error = {
      message: 'Network Error',
    };

    expect(getErrorCode(error)).toBe(CustomErrorCode.CORS);
  });

  it('should return CustomErrorCode.TYPES if error message includes Expecting', () => {
    const error = {
      message: 'Expecting object but received string',
    };

    expect(getErrorCode(error)).toBe(CustomErrorCode.TYPES);
  });

  it('should return CustomErrorCode.UNDEFINED_FIELD if error message includes Cannot read', () => {
    const error = {
      message: 'Cannot read property of undefined',
    };

    expect(getErrorCode(error)).toBe(CustomErrorCode.UNDEFINED_FIELD);
  });

  it('should return CustomErrorCode.UNKNOWN for unknown errors', () => {
    const error = {
      message: 'Some other error',
    };

    expect(getErrorCode(error)).toBe(CustomErrorCode.UNKNOWN);
  });
});

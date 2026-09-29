import { getErrorCode } from './getErrorCode';
import { CustomErrorCode } from 'constants/constants.app';

describe('getErrorCode', () => {
  describe('Axios ошибки со статусом', () => {
    test('для ошибки с status 500 должен вернуть 500', () => {
      const error = {
        isAxiosError: true,
        response: { status: 500 },
      } as any;

      expect(getErrorCode(error)).toBe(500);
    });

    test('для ошибки с status 400 должен вернуть 400', () => {
      const error = {
        isAxiosError: true,
        response: { status: 400 },
      } as any;

      expect(getErrorCode(error)).toBe(400);
    });

    test('для ошибки с status 401 должен вернуть 401', () => {
      const error = {
        isAxiosError: true,
        response: { status: 401 },
      } as any;

      expect(getErrorCode(error)).toBe(401);
    });

    test('для ошибки с status 403 должен вернуть 403', () => {
      const error = {
        isAxiosError: true,
        response: { status: 403 },
      } as any;

      expect(getErrorCode(error)).toBe(403);
    });

    test('для ошибки с status 408 должен вернуть 408', () => {
      const error = {
        isAxiosError: true,
        response: { status: 408 },
      } as any;

      expect(getErrorCode(error)).toBe(408);
    });
  });

  describe('Axios ошибки с таймаутом (code ECONNABORTED)', () => {
    test('для ошибки с code ECONNABORTED должен вернуть таймаут', () => {
      const error = {
        isAxiosError: true,
        code: 'ECONNABORTED',
      } as any;

      expect(getErrorCode(error)).toBe(CustomErrorCode.TIMEOUT);
    });

    test('для ошибки с message содержащим timeout должен вернуть таймаут', () => {
      const error = {
        isAxiosError: true,
        message: 'Request timeout',
      } as any;

      expect(getErrorCode(error)).toBe(CustomErrorCode.TIMEOUT);
    });
  });

  describe('Сетевые ошибки (CORS)', () => {
    test('для ошибки с Network Error должен вернуть CORS', () => {
      const error = new Error('Network Error');

      expect(getErrorCode(error)).toBe(CustomErrorCode.CORS);
    });
  });

  describe('Ошибки парсинга', () => {
    test('для ошибки с Expecting в сообщении должен вернуть TYPES', () => {
      const error = new Error('Expecting value');

      expect(getErrorCode(error)).toBe(CustomErrorCode.TYPES);
    });
  });

  describe('Ошибки доступа к свойствам', () => {
    test('для ошибки с Cannot read в сообщении должен вернуть UNDEFINED_FIELD', () => {
      const error = new Error('Cannot read property of undefined');

      expect(getErrorCode(error)).toBe(CustomErrorCode.UNDEFINED_FIELD);
    });
  });

  describe('Неизвестные ошибки', () => {
    test('для обычной ошибки должен вернуть UNKNOWN', () => {
      const error = new Error('Some error');

      expect(getErrorCode(error)).toBe(CustomErrorCode.UNKNOWN);
    });
  });
});

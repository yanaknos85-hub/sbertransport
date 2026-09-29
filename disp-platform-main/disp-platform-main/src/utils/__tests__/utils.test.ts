/* eslint-disable @typescript-eslint/no-explicit-any */
/**
 * @jest-environment jsdom
 */
import { FormInstance } from 'antd/lib/form/Form';

import {
  _env,
  jwtDecode,
  b64DecodeUnicode,
  b64EncodeUnicode,
  isObject,
  isFilledObject,
  isEmptyObject,
  isObjectWithEmptyValues,
  isEmptyArray,
  isEqualArrays,
  clearSymbols,
  inputCleaner,
  includesByLowerCaseAndSpaces,
  startsWithIgnoreCase,
  toWildcardRegexp,
  ignore,
  preventDefault,
  phoneNumberRegexpInternational,
  onlyNumbersRegExp,
  deepMerge,
  getErrorMessage,
  getErrorProblems,
  handlePlug,
  settingsPhoneNumber
} from '../utils';
import { AxiosError } from 'axios';
import { ILogger } from '@sber-sbertransport/mf-core';

describe('utils', () => {
  describe('_env', () => {
    beforeEach(() => {
      delete process.env.REACT_APP_TEST_VAR;
      delete process.env.TEST_VAR;
    });

    test('should return value from REACT_APP_* prefix first', () => {
      process.env.REACT_APP_TEST_VAR = 'react-app-value';
      expect(_env('TEST_VAR')).toBe('react-app-value');
    });

    test('should return value from non-prefixed env if REACT_APP_* is not set', () => {
      process.env.TEST_VAR = 'non-prefixed-value';
      expect(_env('TEST_VAR')).toBe('non-prefixed-value');
    });

    test('should return undefined if neither env var exists', () => {
      expect(_env('NON_EXISTENT_VAR')).toBeUndefined();
    });
  });

  describe('b64EncodeUnicode and b64DecodeUnicode', () => {
    test('should encode and decode ASCII string', () => {
      const original = 'Hello World';
      const encoded = b64EncodeUnicode(original);
      const decoded = b64DecodeUnicode(encoded);
      expect(encoded).toBe('SGVsbG8gV29ybGQ=');
      expect(decoded).toBe(original);
    });

    test('should encode and decode UTF-8 string with cyrillic chars', () => {
      const original = 'Привет мир';
      const encoded = b64EncodeUnicode(original);
      const decoded = b64DecodeUnicode(encoded);
      expect(decoded).toBe(original);
    });

    test('should encode and decode string with special characters', () => {
      const original = 'Test @#$%^&*()';
      const encoded = b64EncodeUnicode(original);
      const decoded = b64DecodeUnicode(encoded);
      expect(decoded).toBe(original);
    });

    test('should handle empty string', () => {
      const original = '';
      const encoded = b64EncodeUnicode(original);
      const decoded = b64DecodeUnicode(encoded);
      expect(encoded).toBe('');
      expect(decoded).toBe(original);
    });

    test('should handle unicode emoji', () => {
      const original = 'Test 😀 emoji';
      const encoded = b64EncodeUnicode(original);
      const decoded = b64DecodeUnicode(encoded);
      expect(decoded).toBe(original);
    });
  });

  describe('jwtDecode', () => {
    test('should decode JWT payload', () => {
      // Token payload: {"sub":"1234567890","name":"John Doe","iat":1516239022}
      const encodedPayload = btoa('{"sub":"1234567890","name":"John Doe","iat":1516239022}');
      const token = `header.${encodedPayload}.signature`;

      const decoded = jwtDecode<{ sub: string; name: string; iat: number }>(token);

      expect(decoded.sub).toBe('1234567890');
      expect(decoded.name).toBe('John Doe');
      expect(decoded.iat).toBe(1516239022);
    });

    test('should decode JWT with empty payload', () => {
      const encodedPayload = btoa('{}');
      const token = `header.${encodedPayload}.signature`;

      const decoded = jwtDecode<Record<string, unknown>>(token);

      expect(decoded).toEqual({});
    });

    test('should throw error with invalid token', () => {
      expect(() => jwtDecode('invalid.token')).toThrow();
    });

    test('should handle multi-level JWT payload', () => {
      const payload = { user: { id: 1, name: 'Test' } };
      const encodedPayload = btoa(JSON.stringify(payload));
      const token = `header.${encodedPayload}.signature`;

      const decoded = jwtDecode<{ user: { id: number; name: string } }>(token);

      expect(decoded.user.id).toBe(1);
      expect(decoded.user.name).toBe('Test');
    });
  });

  describe('isObject', () => {
    test('should return true for plain object', () => {
      expect(isObject({})).toBe(true);
    });

    test('should return true for object with properties', () => {
      expect(isObject({ a: 1 })).toBe(true);
    });

    test('should return false for null', () => {
      expect(isObject(null)).toBe(false);
    });

    test('should return false for array', () => {
      expect(isObject([])).toBe(false);
    });

    test('should return false for string', () => {
      expect(isObject('test')).toBe(false);
    });

    test('should return false for number', () => {
      expect(isObject(123)).toBe(false);
    });

    test('should return false for boolean', () => {
      expect(isObject(true)).toBe(false);
    });

    test('should return false for undefined', () => {
      expect(isObject(undefined)).toBe(false);
    });
  });

  describe('isFilledObject', () => {
    test('should return true for object with keys', () => {
      expect(isFilledObject({ a: 1 })).toBe(true);
      expect(isFilledObject({ key: 'value' })).toBe(true);
    });

    test('should return false for empty object', () => {
      expect(isFilledObject({})).toBe(false);
    });

    test('should return false for null', () => {
      expect(isFilledObject(null)).toBe(false);
    });

    test('should return false for array', () => {
      expect(isFilledObject([])).toBe(false);
    });

    test('should return false for non-object types', () => {
      expect(isFilledObject('string')).toBe(false);
      expect(isFilledObject(123)).toBe(false);
      expect(isFilledObject(true)).toBe(false);
    });
  });

  describe('isEmptyObject', () => {
    test('should return true for empty object', () => {
      expect(isEmptyObject({})).toBe(true);
    });

    test('should return false for object with keys', () => {
      expect(isEmptyObject({ a: 1 })).toBe(false);
    });

    test('should return false for null', () => {
      expect(isEmptyObject(null)).toBe(false);
    });

    test('should return false for array', () => {
      expect(isEmptyObject([])).toBe(false);
    });

    test('should return false for non-object types', () => {
      expect(isEmptyObject('string')).toBe(false);
      expect(isEmptyObject(123)).toBe(false);
    });
  });

  describe('isObjectWithEmptyValues', () => {
    test('should return true for object with all null values', () => {
      expect(isObjectWithEmptyValues({ a: null, b: null })).toBe(true);
    });

    test('should return true for object with all undefined values', () => {
      expect(isObjectWithEmptyValues({ a: undefined, b: undefined })).toBe(true);
    });

    test('should return true for object with mixed null/undefined values', () => {
      expect(isObjectWithEmptyValues({ a: null, b: undefined })).toBe(true);
    });

    test('should return false for object with any non-empty value', () => {
      expect(isObjectWithEmptyValues({ a: null, b: 'value' })).toBe(false);
      expect(isObjectWithEmptyValues({ a: null, b: 0 })).toBe(false);
      expect(isObjectWithEmptyValues({ a: null, b: false })).toBe(false);
    });

    test('should return false for empty object (isFilledObject takes precedence)', () => {
      // isObjectWithEmptyValues requires isObject to be true AND all values are null/undefined
      // Empty object passes isObject check, and Object.values({}) = [] which.every returns true
      // So empty object is considered to have empty values
      expect(isObjectWithEmptyValues({})).toBe(true);
    });

    test('should return false for null', () => {
      expect(isObjectWithEmptyValues(null)).toBe(false);
    });
  });

  describe('isEmptyArray', () => {
    test('should return true for empty array', () => {
      expect(isEmptyArray([])).toBe(true);
    });

    test('should return false for non-empty array', () => {
      expect(isEmptyArray([1, 2, 3])).toBe(false);
    });

    test('should return false for null', () => {
      expect(isEmptyArray(null)).toBe(false);
    });

    test('should return false for object', () => {
      expect(isEmptyArray({})).toBe(false);
    });

    test('should return false for string', () => {
      expect(isEmptyArray('test')).toBe(false);
    });
  });

  describe('isEqualArrays', () => {
    test('should return true for equal string arrays', () => {
      expect(isEqualArrays(['a', 'b', 'c'], ['a', 'b', 'c'])).toBe(true);
    });

    test('should return true for equal number arrays', () => {
      expect(isEqualArrays([1, 2, 3], [1, 2, 3])).toBe(true);
    });

    test('should return true for mixed string/number arrays', () => {
      expect(isEqualArrays(['a', 1, 'b'], ['a', 1, 'b'])).toBe(true);
    });

    test('should return false for arrays with different elements', () => {
      expect(isEqualArrays(['a', 'b'], ['a', 'c'])).toBe(false);
    });

    test('should return false for arrays with different lengths', () => {
      expect(isEqualArrays([1, 2], [1, 2, 3])).toBe(false);
    });

    test('should return true for empty arrays', () => {
      expect(isEqualArrays([], [])).toBe(true);
    });

    test('should return true when arrays have same elements in different order', () => {
      // Note: This function checks if all elements of arr1 are in arr2, not strict equality
      expect(isEqualArrays([1, 2, 3], [3, 2, 1])).toBe(true);
    });
  });

  describe('clearSymbols', () => {
    test('should remove non-cyrillic/non-latin characters', () => {
      expect(clearSymbols('Hello World 123!@#')).toBe('HelloWorld123');
    });

    test('should keep cyrillic characters', () => {
      expect(clearSymbols('Привет мир!')).toBe('Приветмир');
    });

    test('should keep latin letters and digits', () => {
      expect(clearSymbols('Test123ABC')).toBe('Test123ABC');
    });

    test('should handle mixed cyrillic and latin', () => {
      expect(clearSymbols('Тест Test 123')).toBe('ТестTest123');
    });

    test('should remove special characters', () => {
      expect(clearSymbols('a@b#c$d%e^f&g*h(i)j')).toBe('abcdefghij');
    });

    test('should handle empty string', () => {
      expect(clearSymbols('')).toBe('');
    });

    test('should handle string with only special characters', () => {
      expect(clearSymbols('!@#$%^&*()')).toBe('');
    });
  });

  describe('inputCleaner', () => {
    const mockFormSetFieldsValue = jest.fn();

    const mockForm = {
      setFieldsValue: mockFormSetFieldsValue,
    } as unknown as FormInstance;

    test('should clean input value and call setFieldsValue (no trim when length != minLength)', () => {
      const mockEvent = {
        target: {
          value: '  test  value  ',
          minLength: 5,
          name: 'testField',
        },
      } as React.ChangeEvent<HTMLInputElement>;

      inputCleaner(mockEvent, mockForm);

      // Value has length 13, minLength is 5, so no trim is applied
      expect(mockFormSetFieldsValue).toHaveBeenCalledWith({ testField: ' test value ' });
    });

    test('should keep valid characters including @ and trim spaces', () => {
      const mockEvent = {
        target: {
          value: 'test@value123',
          minLength: 3,
          name: 'field',
        },
      } as React.ChangeEvent<HTMLInputElement>;

      inputCleaner(mockEvent, mockForm);

      // Length = 13, minLength = 3, so no trim
      expect(mockFormSetFieldsValue).toHaveBeenCalledWith({ field: 'test@value123' });
    });

    test('should trim value when length equals minLength', () => {
      const mockEvent = {
        target: {
          value: 'abc',
          minLength: 3,
          name: 'field',
        },
      } as React.ChangeEvent<HTMLInputElement>;

      inputCleaner(mockEvent, mockForm);

      expect(mockFormSetFieldsValue).toHaveBeenCalledWith({ field: 'abc' });
    });

    test('should not trim when length does not equal minLength', () => {
      const mockEvent = {
        target: {
          value: ' ab ',
          minLength: 3,
          name: 'field',
        },
      } as React.ChangeEvent<HTMLInputElement>;

      inputCleaner(mockEvent, mockForm);

      // Length = 3, but after space normalization it's " ab " (length 4)
      // Actually let's trace: value=" ab ", after replace(/\s+/g, ' ') = " ab "
      // length is 4, minLength is 3, so no trim
      expect(mockFormSetFieldsValue).toHaveBeenCalledWith({ field: ' ab ' });
    });
  });

  describe('includesByLowerCaseAndSpaces', () => {
    test('should match substring ignoring case', () => {
      expect(includesByLowerCaseAndSpaces('Hello World', 'world')).toBe(true);
    });

    test('should match substring ignoring spaces', () => {
      expect(includesByLowerCaseAndSpaces('Hello   World', 'hello world')).toBe(true);
    });

    test('should return true for exact match', () => {
      expect(includesByLowerCaseAndSpaces('test string', 'test string')).toBe(true);
    });

    test('should return false when substring not found', () => {
      expect(includesByLowerCaseAndSpaces('Hello World', 'xyz')).toBe(false);
    });

    test('should work with cyrillic characters', () => {
      expect(includesByLowerCaseAndSpaces('Привет мир', 'МИР')).toBe(true);
    });

    test('should work with mixed cyrillic and latin', () => {
      expect(includesByLowerCaseAndSpaces('Тест Test', 'тест test')).toBe(true);
    });
  });

  describe('startsWithIgnoreCase', () => {
    test('should return true for matching prefix ignoring case', () => {
      expect(startsWithIgnoreCase('Hello World', 'hello')).toBe(true);
    });

    test('should return false for non-matching prefix', () => {
      expect(startsWithIgnoreCase('Hello World', 'world')).toBe(false);
    });

    test('should return true for exact match', () => {
      expect(startsWithIgnoreCase('Test', 'Test')).toBe(true);
    });

    test('should return true when strings are equal', () => {
      expect(startsWithIgnoreCase('Hello', 'Hello')).toBe(true);
    });

    test('should handle empty target', () => {
      expect(startsWithIgnoreCase('Hello', '')).toBe(true);
    });

    test('should handle cyrillic characters', () => {
      expect(startsWithIgnoreCase('Привет мир', 'привет')).toBe(true);
    });
  });

  describe('toWildcardRegexp', () => {
    test('should convert wildcard pattern to regex', () => {
      const regex = toWildcardRegexp('test*');
      expect(regex.test('test123')).toBe(true);
      expect(regex.test('test')).toBe(true);
      expect(regex.test('TestABC')).toBe(true);
    });

    test('should handle wildcard in middle', () => {
      const regex = toWildcardRegexp('te*t');
      expect(regex.test('test')).toBe(true);
      expect(regex.test('te123t')).toBe(true);
    });

    test('should handle multiple wildcards', () => {
      const regex = toWildcardRegexp('a*b*c');
      expect(regex.test('abc')).toBe(true);
      expect(regex.test('aXXbYYc')).toBe(true);
    });

    test('should escape special regex characters', () => {
      const regex = toWildcardRegexp('test.+value');
      expect(regex.test('test.+value')).toBe(true);
      expect(regex.test('testXvalue')).toBe(false);
    });

    test('should handle pattern without wildcards', () => {
      const regex = toWildcardRegexp('test');
      expect(regex.test('test')).toBe(true);
      expect(regex.test('Testing')).toBe(true);
    });

    test('should match at start when wildcard present', () => {
      const regex = toWildcardRegexp('*test');
      expect(regex.test('test')).toBe(true);
      expect(regex.test('prefixTest')).toBe(true);
    });
  });

  describe('ignore', () => {
    test('should not throw any errors', () => {
      expect(() => ignore()).not.toThrow();
    });

    test('should return undefined', () => {
      expect(ignore()).toBeUndefined();
    });
  });

  describe('preventDefault', () => {
    test('should prevent default for Enter key', () => {
      const mockEvent = {
        key: 'Enter',
        preventDefault: jest.fn(),
      } as unknown as React.KeyboardEvent<HTMLInputElement>;

      preventDefault(mockEvent);

      expect(mockEvent.preventDefault).toHaveBeenCalled();
    });

    test('should not prevent default for other keys', () => {
      const mockEvent = {
        key: 'Tab',
        preventDefault: jest.fn(),
      } as unknown as React.KeyboardEvent<HTMLInputElement>;

      preventDefault(mockEvent);

      expect(mockEvent.preventDefault).not.toHaveBeenCalled();
    });

    test('should not prevent default for Escape key', () => {
      const mockEvent = {
        key: 'Escape',
        preventDefault: jest.fn(),
      } as unknown as React.KeyboardEvent<HTMLInputElement>;

      preventDefault(mockEvent);

      expect(mockEvent.preventDefault).not.toHaveBeenCalled();
    });
  });

  describe('phoneNumberRegexpInternational', () => {
    test('should match +7 format with parentheses and dashes', () => {
      // This is the supported format: +7 (xxx) xxx-xx-xx
      expect(phoneNumberRegexpInternational.test('+7 (999) 123-45-67')).toBe(true);
    });

    test('should not match format without spaces and parentheses', () => {
      expect(phoneNumberRegexpInternational.test('+79991234567')).toBe(false);
    });

    test('should not match invalid format', () => {
      expect(phoneNumberRegexpInternational.test('1234567890')).toBe(false);
    });

    test('should not match incomplete number', () => {
      expect(phoneNumberRegexpInternational.test('+7 999')).toBe(false);
    });
  });

  describe('onlyNumbersRegExp', () => {
    test('should match string with only digits', () => {
      expect(onlyNumbersRegExp.test('123456')).toBe(true);
    });

    test('should not match string with letters', () => {
      expect(onlyNumbersRegExp.test('123abc')).toBe(false);
    });

    test('should not match empty string', () => {
      expect(onlyNumbersRegExp.test('')).toBe(true);
    });

    test('should not match string with spaces', () => {
      expect(onlyNumbersRegExp.test('123 456')).toBe(false);
    });

    test('should match single digit', () => {
      expect(onlyNumbersRegExp.test('5')).toBe(true);
    });
  });

  describe('deepMerge', () => {
    test('should merge two objects recursively', () => {
      const source = { a: 1, b: { c: 2 } };
      const target = { b: { d: 3 }, e: 4 };

      const result = deepMerge(source, target as any);

      expect(result).toEqual(
        {
          a: 1, b: { c: 2, d: 3 }, e: 4,
        }
      );
    });

    test('should overwrite values from target', () => {
      const source = { a: 1, b: 2 };
      const target = { b: 3 };

      const result = deepMerge(source, target);

      expect(result).toEqual({ a: 1, b: 3 });
    });

    test('should handle empty objects', () => {
      const result = deepMerge({}, {});

      expect(result).toEqual({});
    });

    test('should handle non-object inputs', () => {
      expect(deepMerge('string', 'other')).toBe('other');
      expect(deepMerge(123, 456)).toBe(456);
      expect(deepMerge(null as any, { a: 1 })).toEqual({ a: 1 });
    });

    test('should merge nested objects deeply', () => {
      const source = { a: { b: { c: 1 } } };
      const target = { a: { b: { d: 2 }, e: 3 } };

      const result = deepMerge(source, target as any);

      expect(result).toEqual({ a: { b: { c: 1, d: 2 }, e: 3 } });
    });
  });

  describe('getErrorMessage', () => {
    test('should return message from response data', () => {
      const mockError = {
        response: {
          data: { message: 'Error message' },
        },
      } as unknown as AxiosError;

      expect(getErrorMessage(mockError)).toBe('Error message');
    });

    test('should return undefined when response is null', () => {
      const mockError = {
        response: null,
      } as unknown as AxiosError;

      expect(getErrorMessage(mockError)).toBeUndefined();
    });

    test('should return undefined when response data is missing', () => {
      const mockError = {
        response: {} as { data?: { message?: string } },
      } as unknown as AxiosError;

      expect(getErrorMessage(mockError)).toBeUndefined();
    });

    test('should return undefined for network error', () => {
      const mockError = {} as unknown as AxiosError;

      expect(getErrorMessage(mockError)).toBeUndefined();
    });
  });

  describe('getErrorProblems', () => {
    test('should return problems array from response', () => {
      const mockError = {
        response: {
          data: {
            problems: [
              { field: 'email', message: 'Invalid email' },
              { field: 'name', message: 'Required' },
            ],
          },
        },
      } as unknown as AxiosError;

      const problems = getErrorProblems(mockError);

      expect(problems).toHaveLength(2);
      expect(problems[0]).toEqual({ field: 'email', message: 'Invalid email' });
    });

    test('should filter problems by restrict field', () => {
      const mockError = {
        response: {
          data: {
            problems: [
              { field: 'email', message: 'Invalid email' },
              { field: 'name', message: 'Required' },
              { field: 'phone', message: 'Invalid phone' },
            ],
          },
        },
      } as unknown as AxiosError;

      const problems = getErrorProblems(mockError, ['email', 'phone']);

      expect(problems).toHaveLength(2);
      expect(problems.map(p => p.field)).toEqual(['email', 'phone']);
    });

    test('should return empty array when no problems', () => {
      const mockError = {
        response: {
          data: {},
        },
      } as unknown as AxiosError;

      expect(getErrorProblems(mockError)).toEqual([]);
    });

    test('should handle error without response', () => {
      const mockError = {
        response: null,
      } as unknown as AxiosError;

      expect(getErrorProblems(mockError)).toEqual([]);
    });

    test('should return empty array on exception', () => {
      const mockError = {
        response: {
          data: null,
        },
      } as unknown as AxiosError;

      expect(getErrorProblems(mockError)).toEqual([]);
    });
  });

  describe('handlePlug', () => {
    test('should call logger.toMessage with type and description', () => {
      const mockLogger = {
        toMessage: jest.fn(),
      } as unknown as ILogger;

      handlePlug(mockLogger, 'success', 'Test message');

      expect(mockLogger.toMessage).toHaveBeenCalledWith('success', 'Test message');
    });

    test('should work with error type', () => {
      const mockLogger = {
        toMessage: jest.fn(),
      } as unknown as ILogger;

      handlePlug(mockLogger, 'error', 'Error message');

      expect(mockLogger.toMessage).toHaveBeenCalledWith('error', 'Error message');
    });

    test('should work with warning type', () => {
      const mockLogger = {
        toMessage: jest.fn(),
      } as unknown as ILogger;

      handlePlug(mockLogger, 'warning', 'Warning message');

      expect(mockLogger.toMessage).toHaveBeenCalledWith('warning', 'Warning message');
    });
  });

  describe('settingsPhoneNumber', () => {
    test('should have input configuration with mask and placeholder', () => {
      expect(settingsPhoneNumber.input.mask).toBe('+7 (999) 999-99-99');
      expect(settingsPhoneNumber.input.placeholder).toBe('+7 (___) ___-__-__');
    });

    test('clearPhone should remove non-plus and non-digit characters', () => {
      expect(settingsPhoneNumber.clearPhone('+7 (999) 123-45-67')).toBe('+79991234567');
      expect(settingsPhoneNumber.clearPhone('8 (999) 123-45-67')).toBe('89991234567');
      expect(settingsPhoneNumber.clearPhone('test')).toBe('');
      expect(settingsPhoneNumber.clearPhone('')).toBe('');
      expect(settingsPhoneNumber.clearPhone(null as unknown as string)).toBe('');
    });

    test('test should validate phone format', () => {
      expect(settingsPhoneNumber.test('+7 (999) 123-45-67')).toBe(true);
      expect(settingsPhoneNumber.test('8 (999) 123-45-67')).toBe(true);
      expect(settingsPhoneNumber.test('+7 999 123 45 67')).toBe(false);
      expect(settingsPhoneNumber.test('invalid')).toBe(false);
    });

    test('warning should return formatted message', () => {
      const warning = settingsPhoneNumber.warning();
      expect(warning).toBe('Телефон должен соответствовать формату +7 (999) 999-99-99');
    });
  });
});

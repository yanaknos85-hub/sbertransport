/**
 * @jest-environment jsdom
 */
import * as t from 'io-ts';
import * as Either from 'fp-ts/lib/Either';
import moment from 'moment';

import * as ioTsUtils from '../../io-ts';

describe('io-ts utils', () => {
  describe('optional', () => {
    test('should accept valid value', () => {
      const codec = ioTsUtils.optional(t.string);
      const result = codec.decode('test');

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBe('test');
      }
    });

    test('should accept undefined', () => {
      const codec = ioTsUtils.optional(t.string);
      const result = codec.decode(undefined);

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBeUndefined();
      }
    });

    test('should reject null (optional only accepts undefined, not null)', () => {
      const codec = ioTsUtils.optional(t.string);
      const result = codec.decode(null);

      expect(Either.isLeft(result)).toBe(true);
    });

    test('should reject invalid value', () => {
      const codec = ioTsUtils.optional(t.string);
      const result = codec.decode(123);

      expect(Either.isLeft(result)).toBe(true);
    });
  });

  describe('withValidate', () => {
    test('should accept valid value with custom validation', () => {
      const codec = ioTsUtils.withValidate(t.string, (u, _c) => {
        if (typeof u === 'string' && u.length > 0) {
          return t.success(u);
        }
        return t.failure(u, _c);
      }, 'NonEmptyString');

      const result = codec.decode('test');

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBe('test');
      }
    });

    test('should reject invalid value with custom validation', () => {
      const codec = ioTsUtils.withValidate(t.string, (u, _c) => {
        if (typeof u === 'string' && u.length > 0) {
          return t.success(u);
        }
        return t.failure(u, _c);
      }, 'NonEmptyString');

      const result = codec.decode('');

      expect(Either.isLeft(result)).toBe(true);
    });

    test('should use codec name as default', () => {
      const codec = ioTsUtils.withValidate(t.string, (_u, _c) => t.success(_u as string));

      expect(codec.name).toBe('string');
    });
  });

  describe('fallback', () => {
    test('should return valid value when decode succeeds', () => {
      const codec = ioTsUtils.fallback(t.string, 'default');
      const result = codec.decode('test');

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBe('test');
      }
    });

    test('should return fallback value when decode fails', () => {
      const codec = ioTsUtils.fallback(t.string, 'default');

      const result = codec.decode(123);

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBe('default');
      }
    });

    test('should use default name when not specified', () => {
      const codec = ioTsUtils.fallback(t.string, 'default');

      expect(codec.name).toBe('withFallback(string)');
    });
  });

  describe('numberString', () => {
    test('should decode valid numeric string to number', () => {
      const result = ioTsUtils.numberString.decode('123');

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBe(123);
      }
    });

    test('should decode negative numeric string to number', () => {
      const result = ioTsUtils.numberString.decode('-456');

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBe(-456);
      }
    });

    test('should decode zero from string', () => {
      const result = ioTsUtils.numberString.decode('0');

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBe(0);
      }
    });

    test('should reject NaN string', () => {
      const result = ioTsUtils.numberString.decode('abc');

      expect(Either.isLeft(result)).toBe(true);
    });

    test('should reject empty string', () => {
      const result = ioTsUtils.numberString.decode('');

      expect(Either.isLeft(result)).toBe(true);
    });

    test('should reject whitespace-only string', () => {
      const result = ioTsUtils.numberString.decode('   ');

      expect(Either.isLeft(result)).toBe(true);
    });

    test('should encode number to string', () => {
      const result = ioTsUtils.numberString.encode(123);

      expect(result).toBe('123');
    });

    test('should encode negative number to string', () => {
      const result = ioTsUtils.numberString.encode(-456);

      expect(result).toBe('-456');
    });

    test('should encode zero to string', () => {
      const result = ioTsUtils.numberString.encode(0);

      expect(result).toBe('0');
    });
  });

  describe('money', () => {
    test('should decode kopeks to rubles', () => {
      const result = ioTsUtils.money.decode(1000);

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBe(10);
      }
    });

    test('should decode kopeks with decimal result', () => {
      const result = ioTsUtils.money.decode(150);

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBe(1.5);
      }
    });

    test('should encode rubles to kopeks', () => {
      const result = ioTsUtils.money.encode(10);

      expect(result).toBe(1000);
    });

    test('should encode rubles with decimal to kopeks', () => {
      const result = ioTsUtils.money.encode(1.5);

      expect(result).toBe(150);
    });

    test('should handle zero', () => {
      expect(ioTsUtils.money.decode(0)).toEqual(Either.right(0));
      expect(ioTsUtils.money.encode(0)).toBe(0);
    });

    test('should handle negative values (debt)', () => {
      const result = ioTsUtils.money.decode(-100);

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBe(-1);
      }
    });
  });

  describe('mobilePhone', () => {
    test('should validate and format phone with spaces', () => {
      const result = ioTsUtils.mobilePhone.decode('+7 999 123-45-67');

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBe('+79991234567');
      }
    });

    test('should validate and format phone with dashes', () => {
      const result = ioTsUtils.mobilePhone.decode('+7-999-123-45-67');

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBe('+79991234567');
      }
    });

    test('should validate and format phone with parentheses', () => {
      const result = ioTsUtils.mobilePhone.decode('+7 (999) 123-45-67');

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBe('+79991234567');
      }
    });

    test('should validate and format phone with dots', () => {
      const result = ioTsUtils.mobilePhone.decode('+7.999.123-45-67');

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBe('+79991234567');
      }
    });

    test('should validate and format phone without country code', () => {
      const result = ioTsUtils.mobilePhone.decode('9991234567');

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBe('+9991234567');
      }
    });

    test('should reject non-string input', () => {
      const result = ioTsUtils.mobilePhone.decode(123);

      expect(Either.isLeft(result)).toBe(true);
    });

    test('should handle empty string', () => {
      const result = ioTsUtils.mobilePhone.decode('');

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBe('+');
      }
    });

    test('should encode string (identity for valid input)', () => {
      const result = ioTsUtils.mobilePhone.encode('+79991234567');

      expect(result).toBe('+79991234567');
    });
  });

  describe('nullable', () => {
    test('should accept valid value', () => {
      const codec = ioTsUtils.nullable(t.string);
      const result = codec.decode('test');

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBe('test');
      }
    });

    test('should accept null', () => {
      const codec = ioTsUtils.nullable(t.string);
      const result = codec.decode(null);

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBeNull();
      }
    });

    test('should reject undefined', () => {
      const codec = ioTsUtils.nullable(t.string);
      const result = codec.decode(undefined);

      expect(Either.isLeft(result)).toBe(true);
    });

    test('should reject invalid value', () => {
      const codec = ioTsUtils.nullable(t.string);
      const result = codec.decode(123);

      expect(Either.isLeft(result)).toBe(true);
    });
  });

  describe('epochTimestamp', () => {
    test('should decode timestamp to Date', () => {
      const timestamp = 1609459200000; // 2021-01-01 00:00:00 UTC
      const result = ioTsUtils.epochTimestamp.decode(timestamp);

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBeInstanceOf(Date);
        expect(result.right.getTime()).toBe(timestamp);
      }
    });

    test('should reject non-number input', () => {
      const result = ioTsUtils.epochTimestamp.decode('1609459200000');

      expect(Either.isLeft(result)).toBe(true);
    });

    test('should encode Date to timestamp', () => {
      const date = new Date(1609459200000);
      const result = ioTsUtils.epochTimestamp.encode(date);

      expect(result).toBe(1609459200000);
    });
  });

  describe('time', () => {
    test('should decode milliseconds to minutes', () => {
      const result = ioTsUtils.time.decode(60000);

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBe(1);
      }
    });

    test('should decode milliseconds with decimal result', () => {
      const result = ioTsUtils.time.decode(90000);

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBe(1.5);
      }
    });

    test('should encode minutes to milliseconds', () => {
      const result = ioTsUtils.time.encode(1);

      expect(result).toBe(60000);
    });

    test('should encode decimal minutes to milliseconds', () => {
      const result = ioTsUtils.time.encode(1.5);

      expect(result).toBe(90000);
    });

    test('should handle zero', () => {
      expect(ioTsUtils.time.decode(0)).toEqual(Either.right(0));
      expect(ioTsUtils.time.encode(0)).toBe(0);
    });

    test('should handle negative values', () => {
      const result = ioTsUtils.time.decode(-60000);

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBe(-1);
      }
    });
  });

  describe('ISODate', () => {
    test('should decode valid ISO string to moment', () => {
      const result = ioTsUtils.ISODate.decode('2021-01-01T00:00:00.000Z');

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBeInstanceOf(moment);
      }
    });

    test('should reject invalid ISO string', () => {
      const result = ioTsUtils.ISODate.decode('not-a-date');

      expect(Either.isLeft(result)).toBe(true);
    });

    test('should reject non-string input', () => {
      const result = ioTsUtils.ISODate.decode(123);

      expect(Either.isLeft(result)).toBe(true);
    });

    test('should encode moment to ISO string', () => {
      const date = moment('2021-01-01T00:00:00.000Z');
      const result = ioTsUtils.ISODate.encode(date);

      expect(result).toBe('2021-01-01T00:00:00.000Z');
    });

    test('should handle date with time component', () => {
      const result = ioTsUtils.ISODate.decode('2021-06-15T14:30:45.123Z');

      expect(Either.isRight(result)).toBe(true);
    });
  });

  describe('EpochMS', () => {
    test('should decode timestamp to moment', () => {
      const timestamp = 1609459200000;
      const result = ioTsUtils.EpochMS.decode(timestamp);

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBeInstanceOf(moment);
        expect(result.right.valueOf()).toBe(timestamp);
      }
    });

    test('should reject non-number input', () => {
      const result = ioTsUtils.EpochMS.decode('1609459200000');

      expect(Either.isLeft(result)).toBe(true);
    });

    test('should encode moment to timestamp', () => {
      const date = moment(1609459200000);
      const result = ioTsUtils.EpochMS.encode(date);

      expect(result).toBe(1609459200000);
    });
  });

  describe('uuid', () => {
    test('should accept valid UUID v4', () => {
      const uuidStr = '550e8400-e29b-41d4-a716-446655440000';
      const result = ioTsUtils.uuid.decode(uuidStr);

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBe(uuidStr);
      }
    });

    test('should accept valid UUID v4 uppercase', () => {
      const uuidStr = '550E8400-E29B-41D4-A716-446655440000';
      const result = ioTsUtils.uuid.decode(uuidStr);

      expect(Either.isRight(result)).toBe(true);
    });

    test('should reject invalid UUID (wrong format)', () => {
      const result = ioTsUtils.uuid.decode('not-a-uuid');

      expect(Either.isLeft(result)).toBe(true);
    });

    test('should accept UUID v3 (format validation only)', () => {
      // Version 3 UUID (uses MD5 hashing) - should be accepted since we only validate format
      const result = ioTsUtils.uuid.decode('6fa459ea-ee8a-3ca4-894e-db77e160355e');

      expect(Either.isRight(result)).toBe(true);
    });

    test('should reject non-string input', () => {
      const result = ioTsUtils.uuid.decode(123);

      expect(Either.isLeft(result)).toBe(true);
    });

    test('should accept UUID with mixed case', () => {
      const uuidStr = '550E8400-e29b-41d4-A716-446655440000';
      const result = ioTsUtils.uuid.decode(uuidStr);

      expect(Either.isRight(result)).toBe(true);
    });
  });

  describe('oneOf', () => {
    test('should accept valid literal value', () => {
      const codec = ioTsUtils.oneOf('foo', 'bar', 'baz');

      const result = codec.decode('foo');

      expect(Either.isRight(result)).toBe(true);
    });

    test('should reject invalid literal value', () => {
      const codec = ioTsUtils.oneOf('foo', 'bar', 'baz');

      const result = codec.decode('qux');

      expect(Either.isLeft(result)).toBe(true);
    });

    test('should reject non-string input', () => {
      const codec = ioTsUtils.oneOf('foo', 'bar', 'baz');

      const result = codec.decode(123);

      expect(Either.isLeft(result)).toBe(true);
    });

    test('should accept empty string as valid literal', () => {
      const codec = ioTsUtils.oneOf('', 'bar', 'baz');

      const result = codec.decode('');

      expect(Either.isRight(result)).toBe(true);
    });

    test('should create correct number of union members', () => {
      const codec = ioTsUtils.oneOf('a', 'b', 'c', 'd', 'e');

      expect(codec.types.length).toBe(5);
    });

    test('should work with single literal', () => {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const codec = (ioTsUtils.oneOf as any)('single');

      const result = codec.decode('single');

      expect(Either.isRight(result)).toBe(true);
    });
  });

  describe('DecoderInput', () => {
    test('should extract decoder input type', () => {
      // This is a type-level test, so we just verify the type exists
      const testString: ioTsUtils.DecoderInput<t.Type<string>> = 'test';
      const testNumber: ioTsUtils.DecoderInput<t.Type<number>> = 123;

      expect(testString).toBe('test');
      expect(testNumber).toBe(123);
    });
  });

  describe('exported wrapped from index', () => {
    test('should re-export wrapped from wrapped module', () => {
      // Verify wrapped is available from index
      expect(typeof ioTsUtils.wrapped).toBe('function');
    });
  });
});

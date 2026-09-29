/* eslint-disable @typescript-eslint/no-explicit-any */
import { either as Either } from 'fp-ts';
import { ioTypeFromEnum, isEnumValue } from '../ioTypeFromEnum';

// Mock enum for testing
enum TestEnum {
  OptionA = 'OPTION_A',
  OptionB = 'OPTION_B',
  OptionC = 'OPTION_C',
}

enum NumericEnum {
  One = 1,
  Two = 2,
  Three = 3,
}

enum MixedEnum {
  StringValue = 'STRING',
  NumberValue = 123,
}

describe('ioTypeFromEnum', () => {
  describe('isEnumValue', () => {
    test('should return true for valid string enum value', () => {
      expect(isEnumValue(TestEnum, TestEnum.OptionA)).toBe(true);
      expect(isEnumValue(TestEnum, TestEnum.OptionB)).toBe(true);
      expect(isEnumValue(TestEnum, TestEnum.OptionC)).toBe(true);
    });

    test('should return false for invalid string value', () => {
      expect(isEnumValue(TestEnum, 'INVALID')).toBe(false);
      expect(isEnumValue(TestEnum, 123)).toBe(false);
      expect(isEnumValue(TestEnum, null)).toBe(false);
      expect(isEnumValue(TestEnum, undefined)).toBe(false);
    });

    test('should return true for valid numeric enum value', () => {
      expect(isEnumValue(NumericEnum, NumericEnum.One)).toBe(true);
      expect(isEnumValue(NumericEnum, NumericEnum.Two)).toBe(true);
      expect(isEnumValue(NumericEnum, NumericEnum.Three)).toBe(true);
    });

    test('should return false for invalid numeric value', () => {
      expect(isEnumValue(NumericEnum, 999)).toBe(false);
      expect(isEnumValue(NumericEnum, '1')).toBe(false);
    });

    test('should return true for valid mixed enum value', () => {
      expect(isEnumValue(MixedEnum, MixedEnum.StringValue)).toBe(true);
      expect(isEnumValue(MixedEnum, MixedEnum.NumberValue)).toBe(true);
    });

    test('should return false for invalid mixed enum value', () => {
      expect(isEnumValue(MixedEnum, 'OTHER_STRING')).toBe(false);
      expect(isEnumValue(MixedEnum, 456)).toBe(false);
    });
  });

  describe('ioTypeFromEnum', () => {
    test('should create a valid io-ts type from string enum', () => {
      const enumType = ioTypeFromEnum('TestEnum', TestEnum);

      expect(enumType.name).toBe('TestEnum');
      expect(enumType.is(TestEnum.OptionA)).toBe(true);
      expect(enumType.is(TestEnum.OptionB)).toBe(true);
      expect(enumType.is(TestEnum.OptionC)).toBe(true);
    });

    test('should create a valid io-ts type from numeric enum', () => {
      const enumType = ioTypeFromEnum('NumericEnum', NumericEnum);

      expect(enumType.name).toBe('NumericEnum');
      expect(enumType.is(NumericEnum.One)).toBe(true);
      expect(enumType.is(NumericEnum.Two)).toBe(true);
      expect(enumType.is(NumericEnum.Three)).toBe(true);
    });

    test('should create a valid io-ts type from mixed enum', () => {
      const enumType = ioTypeFromEnum('MixedEnum', MixedEnum);

      expect(enumType.name).toBe('MixedEnum');
      expect(enumType.is(MixedEnum.StringValue)).toBe(true);
      expect(enumType.is(MixedEnum.NumberValue)).toBe(true);
    });

    test('should validate successful decode for valid enum value', () => {
      const enumType = ioTypeFromEnum('TestEnum', TestEnum);
      const result = enumType.decode(TestEnum.OptionA);

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBe(TestEnum.OptionA);
      }
    });

    test('should validate failure for invalid value', () => {
      const enumType = ioTypeFromEnum('TestEnum', TestEnum);
      const result = enumType.decode('INVALID_VALUE' as unknown);

      expect(Either.isLeft(result)).toBe(true);
    });

    test('should validate failure for null', () => {
      const enumType = ioTypeFromEnum('TestEnum', TestEnum);
      const result = enumType.decode(null);

      expect(Either.isLeft(result)).toBe(true);
    });

    test('should validate failure for undefined', () => {
      const enumType = ioTypeFromEnum('TestEnum', TestEnum);
      const result = enumType.decode(undefined);

      expect(Either.isLeft(result)).toBe(true);
    });

    test('should validate failure for number when expecting string enum', () => {
      const enumType = ioTypeFromEnum('TestEnum', TestEnum);
      const result = enumType.decode(123 as unknown);

      expect(Either.isLeft(result)).toBe(true);
    });

    test('should validate failure for object', () => {
      const enumType = ioTypeFromEnum('TestEnum', TestEnum);
      const result = enumType.decode({ key: 'value' } as unknown);

      expect(Either.isLeft(result)).toBe(true);
    });

    test('should validate failure for array', () => {
      const enumType = ioTypeFromEnum('TestEnum', TestEnum);
      const result = enumType.decode([1, 2, 3] as unknown);

      expect(Either.isLeft(result)).toBe(true);
    });

    test('should work with empty string enum values', () => {
      const EmptyStringEnum = {
        Empty: '',
        Value: 'value',
      };

      const enumType = ioTypeFromEnum('EmptyStringEnum', EmptyStringEnum);

      expect(enumType.is('')).toBe(true);
      expect(enumType.is('value')).toBe(true);
    });

    test('should handle enum with duplicate values correctly', () => {
      const DuplicateEnum = {
        A: 'same',
        B: 'same',
        C: 'different',
      };

      const enumType = ioTypeFromEnum('DuplicateEnum', DuplicateEnum);

      expect(enumType.is('same')).toBe(true);
      expect(enumType.is('different')).toBe(true);
    });

    test('should return the correct type name for debugging', () => {
      const customName = 'CustomTypeName';
      const enumType = ioTypeFromEnum(customName, TestEnum);

      expect(enumType.name).toBe(customName);
    });

    test('should encode values correctly (identity)', () => {
      const enumType = ioTypeFromEnum('TestEnum', TestEnum);

      expect(enumType.encode(TestEnum.OptionA)).toBe(TestEnum.OptionA);
      expect(enumType.encode(TestEnum.OptionB)).toBe(TestEnum.OptionB);
    });

    test('should work with numeric enum decoding', () => {
      const enumType = ioTypeFromEnum('NumericEnum', NumericEnum);

      expect(enumType.is(NumericEnum.One)).toBe(true);
      const result = enumType.decode(NumericEnum.One);

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBe(NumericEnum.One);
      }
    });

    test('should validate failure for number not in numeric enum', () => {
      const enumType = ioTypeFromEnum('NumericEnum', NumericEnum);
      const result = enumType.decode(999 as unknown);

      expect(Either.isLeft(result)).toBe(true);
    });
  });
});

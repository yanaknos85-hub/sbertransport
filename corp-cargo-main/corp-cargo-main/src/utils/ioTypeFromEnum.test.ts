import { ioTypeFromEnum } from './ioTypeFromEnum';
import * as t from 'io-ts';

enum StringEnum {
  OPTION_A = 'option_a',
  OPTION_B = 'option_b',
  OPTION_C = 'option_c',
}

enum NumericEnum {
  ZERO = 0,
  ONE = 1,
  TWO = 2,
}

enum MixedEnum {
  STRING_VALUE = 'string',
  NUMBER_VALUE = 100,
}

describe('ioTypeFromEnum', () => {
  describe('строковый enum', () => {
    const stringEnumType = ioTypeFromEnum<StringEnum>('StringEnum', StringEnum);

    test('для валидного значения должен вернуть success', () => {
      const result = stringEnumType.validate(StringEnum.OPTION_A, t.getDefaultContext(stringEnumType));
      expect(result._tag).toBe('Right');
    });

    test('для другого валидного значения должен вернуть success', () => {
      const result = stringEnumType.validate(StringEnum.OPTION_B, t.getDefaultContext(stringEnumType));
      expect(result._tag).toBe('Right');
    });

    test('для несуществующего значения должен вернуть failure', () => {
      const result = stringEnumType.validate('invalid' as unknown as StringEnum, t.getDefaultContext(stringEnumType));
      expect(result._tag).toBe('Left');
    });

    test('для null должен вернуть failure', () => {
      const result = stringEnumType.validate(null, t.getDefaultContext(stringEnumType));
      expect(result._tag).toBe('Left');
    });

    test('для undefined должен вернуть failure', () => {
      const result = stringEnumType.validate(undefined as unknown as StringEnum, t.getDefaultContext(stringEnumType));
      expect(result._tag).toBe('Left');
    });

    test('для пустой строки должен вернуть failure', () => {
      const result = stringEnumType.validate('', t.getDefaultContext(stringEnumType));
      expect(result._tag).toBe('Left');
    });

    test('для числа должен вернуть failure', () => {
      const result = stringEnumType.validate(123, t.getDefaultContext(stringEnumType));
      expect(result._tag).toBe('Left');
    });

    test('для объекта должен вернуть failure', () => {
      const result = stringEnumType.validate({ key: 'value' }, t.getDefaultContext(stringEnumType));
      expect(result._tag).toBe('Left');
    });

    test('is() должен вернуть true для валидного значения', () => {
      expect(stringEnumType.is(StringEnum.OPTION_A)).toBe(true);
    });

    test('is() должен вернуть false для невалидного значения', () => {
      expect(stringEnumType.is('invalid' as unknown as StringEnum)).toBe(false);
    });

    test('is() должен вернуть false для null', () => {
      expect(stringEnumType.is(null)).toBe(false);
    });

    test('decode() должен вернуть валидное значение', () => {
      const result = stringEnumType.decode(StringEnum.OPTION_A);
      expect((result as any).right).toBe(StringEnum.OPTION_A);
    });

    test('encode() должен вернуть значение', () => {
      const result = stringEnumType.encode(StringEnum.OPTION_A);
      expect(result).toBe(StringEnum.OPTION_A);
    });
  });

  describe('числовой enum', () => {
    const numericEnumType = ioTypeFromEnum<NumericEnum>('NumericEnum', NumericEnum);

    test('для валидного значения должен вернуть success', () => {
      const result = numericEnumType.validate(NumericEnum.ZERO, t.getDefaultContext(numericEnumType));
      expect(result._tag).toBe('Right');
    });

    test('для другого валидного значения должен вернуть success', () => {
      const result = numericEnumType.validate(NumericEnum.ONE, t.getDefaultContext(numericEnumType));
      expect(result._tag).toBe('Right');
    });

    test('для несуществующего значения должен вернуть failure', () => {
      const result = numericEnumType.validate(999 as unknown as NumericEnum, t.getDefaultContext(numericEnumType));
      expect(result._tag).toBe('Left');
    });

    test('для null должен вернуть failure', () => {
      const result = numericEnumType.validate(null, t.getDefaultContext(numericEnumType));
      expect(result._tag).toBe('Left');
    });

    test('для undefined должен вернуть failure', () => {
      const result = numericEnumType.validate(undefined as unknown as NumericEnum, t.getDefaultContext(numericEnumType));
      expect(result._tag).toBe('Left');
    });

    test('для плавающей точки должен вернуть failure', () => {
      const result = numericEnumType.validate(1.5, t.getDefaultContext(numericEnumType));
      expect(result._tag).toBe('Left');
    });

    test('is() должен вернуть true для валидного значения', () => {
      expect(numericEnumType.is(NumericEnum.ONE)).toBe(true);
    });

    test('is() должен вернуть false для невалидного значения', () => {
      expect(numericEnumType.is(999 as unknown as NumericEnum)).toBe(false);
    });

    test('decode() должен вернуть валидное значение', () => {
      const result = numericEnumType.decode(NumericEnum.TWO);
      expect((result as any).right).toBe(NumericEnum.TWO);
    });

    test('encode() должен вернуть значение', () => {
      const result = numericEnumType.encode(NumericEnum.ZERO);
      expect(result).toBe(NumericEnum.ZERO);
    });
  });

  describe('смешанный enum (строки и числа)', () => {
    const mixedEnumType = ioTypeFromEnum<MixedEnum>('MixedEnum', MixedEnum);

    test('для строкового значения должен вернуть success', () => {
      const result = mixedEnumType.validate(MixedEnum.STRING_VALUE, t.getDefaultContext(mixedEnumType));
      expect(result._tag).toBe('Right');
    });

    test('для числового значения должен вернуть success', () => {
      const result = mixedEnumType.validate(MixedEnum.NUMBER_VALUE, t.getDefaultContext(mixedEnumType));
      expect(result._tag).toBe('Right');
    });

    test('другое строковое значение должно быть невалидным', () => {
      const result = mixedEnumType.validate('other' as unknown as MixedEnum, t.getDefaultContext(mixedEnumType));
      expect(result._tag).toBe('Left');
    });

    test('другое числовое значение должно быть невалидным', () => {
      const result = mixedEnumType.validate(200 as unknown as MixedEnum, t.getDefaultContext(mixedEnumType));
      expect(result._tag).toBe('Left');
    });

    test('is() должен вернуть true для строкового значения', () => {
      expect(mixedEnumType.is(MixedEnum.STRING_VALUE)).toBe(true);
    });

    test('is() должен вернуть true для числового значения', () => {
      expect(mixedEnumType.is(MixedEnum.NUMBER_VALUE)).toBe(true);
    });

    test('is() должен вернуть false для других значений', () => {
      expect(mixedEnumType.is(300 as unknown as MixedEnum)).toBe(false);
    });
  });

  describe('пустой enum', () => {
    const emptyEnum = {} as Record<string, string | number>;
    const emptyEnumType = ioTypeFromEnum('EmptyEnum', emptyEnum);

    test('для любого значения должен вернуть failure', () => {
      const result = emptyEnumType.validate('any' as unknown as never, t.getDefaultContext(emptyEnumType));
      expect(result._tag).toBe('Left');
    });

    test('is() должен вернуть false для любого значения', () => {
      expect(emptyEnumType.is('any' as unknown as never)).toBe(false);
    });
  });

  describe('возвращаемый тип', () => {
    test('должен иметь имя enum', () => {
      const type = ioTypeFromEnum<StringEnum>('CustomName', StringEnum);
      expect(type.name).toBe('CustomName');
    });
  });
});

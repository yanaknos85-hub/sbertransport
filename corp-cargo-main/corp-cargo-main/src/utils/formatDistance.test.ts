import { formatDistance, formatDistanceValue } from './formatDistance';

describe('formatDistance', () => {
  describe('formatDistance', () => {
    test('для конечного числа должен форматировать с группировкой разрядов', () => {
      expect(formatDistance(1000)).toBe('1\u00A0000');
      expect(formatDistance(10000)).toBe('10\u00A0000');
      expect(formatDistance(1000000)).toBe('1\u00A0000\u00A0000');
    });

    test('для отрицательного числа должен форматировать correctly', () => {
      expect(formatDistance(-1000)).toBe('-1\u00A0000');
      expect(formatDistance(-50000)).toBe('-50\u00A0000');
    });

    test('для нуля должен вернуть "0"', () => {
      expect(formatDistance(0)).toBe('0');
    });

    test('для undefined должен вернуть значение по умолчанию "-"', () => {
      expect(formatDistance(undefined)).toBe('-');
    });

    test('для null должен вернуть значение по умолчанию "-"', () => {
      expect(formatDistance(null)).toBe('-');
    });

    test('для NaN должен вернуть значение по умолчанию "-"', () => {
      expect(formatDistance(NaN)).toBe('-');
    });

    test('для Infinity должен вернуть значение по умолчанию "-"', () => {
      expect(formatDistance(Infinity)).toBe('-');
    });

    test('для числа с плавающей точкой должен форматировать полностью', () => {
      expect(formatDistance(1234.56)).toBe('1\u00A0234,56');
    });

    test('для кастомного emptyValue должен использовать его', () => {
      expect(formatDistance(undefined, '')).toBe('');
      expect(formatDistance(undefined, 'N/A')).toBe('N/A');
      expect(formatDistance(null, '—')).toBe('—');
    });

    test('для очень больших чисел должен форматировать правильно', () => {
      expect(formatDistance(999999999)).toBe('999\u00A0999\u00A0999');
    });

    test('для очень маленьких чисел должен форматировать полностью', () => {
      expect(formatDistance(0.001)).toBe('0,001');
    });
  });

  describe('formatDistanceValue', () => {
    test('для целого числа должен вернуть строку с запятой', () => {
      expect(formatDistanceValue(1000)).toBe('1000');
      expect(formatDistanceValue(0)).toBe('0');
      expect(formatDistanceValue(-500)).toBe('-500');
    });

    test('для числа с одной цифрой после запятой должен оставить её', () => {
      expect(formatDistanceValue(1234.5)).toBe('1234,5');
      expect(formatDistanceValue(0.1)).toBe('0,1');
      expect(formatDistanceValue(-123.7)).toBe('-123,7');
    });

    test('для числа с несколькими цифрами после запятой должен округлить до одной', () => {
      expect(formatDistanceValue(1234.56)).toBe('1234,6');
      expect(formatDistanceValue(1234.54)).toBe('1234,5');
      expect(formatDistanceValue(1234.55)).toBe('1234,6');
    });

    test('для очень маленьких чисел должен округлить правильно', () => {
      expect(formatDistanceValue(0.01)).toBe('0');
      expect(formatDistanceValue(0.05)).toBe('0,1');
      expect(formatDistanceValue(0.04)).toBe('0');
    });

    test('для отрицательных чисел с дробной частью должен правильно округлить', () => {
      expect(formatDistanceValue(-123.56)).toBe('-123,6');
      expect(formatDistanceValue(-123.54)).toBe('-123,5');
    });

    test('для чисел без дробной части должен вернуть целую часть', () => {
      expect(formatDistanceValue(1000.0)).toBe('1000');
      expect(formatDistanceValue(-500.0)).toBe('-500');
    });
  });


  describe('formatDistanceNumbers', () => {
    test('для целого числа должен форматировать полностью', () => {
      expect(formatDistance(1000)).toBe('1\u00A0000');
      expect(formatDistance(50000)).toBe('50\u00A0000');
      expect(formatDistance(-1000)).toBe('-1\u00A0000');
    });
    test('для числа с дробной частью должен форматировать полностью', () => {
      expect(formatDistance(1234.56)).toBe('1\u00A0234,56');
      expect(formatDistance(-1234.56)).toBe('-1\u00A0234,56');
      expect(formatDistance(1234.5)).toBe('1\u00A0234,5');
    });
    test('для числа с дробной частью меньше 0.1 должен форматировать полностью', () => {
      expect(formatDistance(0.05)).toBe('0,05');
      expect(formatDistance(-0.05)).toBe('-0,05');
      expect(formatDistance(0.005)).toBe('0,005');
    });
  });
});

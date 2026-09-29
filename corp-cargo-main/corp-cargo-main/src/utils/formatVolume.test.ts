import { formatVolume } from './formatVolume';

describe('formatVolume', () => {
  describe('обработка null/undefined', () => {
    test('для null должен вернуть "-"', () => {
      expect(formatVolume(null)).toBe('-');
    });

    test('для undefined должен вернуть "-"', () => {
      // @ts-expect-error: testing with undefined
      expect(formatVolume(undefined)).toBe('-');
    });
  });

  describe('обработка нуля', () => {
    test('для 0 должен вернуть "-"', () => {
      expect(formatVolume(0)).toBe('-');
    });
  });

  describe('минимальное значение (меньше MIN_VALUE)', () => {
    test('для очень маленького числа должен вернуть 0.001', () => {
      expect(formatVolume(0.0001)).toBe('0.001');
    });

    test('для числа меньше MIN_VALUE в м³ должен вернуть 0.001', () => {
      expect(formatVolume(500)).toBe('0.001');
      expect(formatVolume(999)).toBe('0.001');
    });

    test('для 1 должен вернуть 0.001', () => {
      expect(formatVolume(1)).toBe('0.001');
    });

    test('для 500 должен вернуть 0.001', () => {
      expect(formatVolume(500)).toBe('0.001');
    });
  });

  describe('обычные значения (равны или больше MIN_VALUE)', () => {
    test('для 1000000 (1 м³) должен вернуть 1.000', () => {
      expect(formatVolume(1000000)).toBe('1.000');
    });

    test('для 2000000 (2 м³) должен вернуть 2.000', () => {
      expect(formatVolume(2000000)).toBe('2.000');
    });

    test('для 1500000 (1.5 м³) должен вернуть 1.500', () => {
      expect(formatVolume(1500000)).toBe('1.500');
    });

    test('для 1234567 должен вернуть 1.235 с округлением', () => {
      expect(formatVolume(1234567)).toBe('1.235');
    });
  });

  describe('большие значения', () => {
    test('для 1000000000 (1000 м³) должен вернуть 1000.000', () => {
      expect(formatVolume(1000000000)).toBe('1000.000');
    });

    test('для 1234567890 должен вернуть 1234.568 с округлением', () => {
      expect(formatVolume(1234567890)).toBe('1234.568');
    });
  });

  describe('граница MIN_VALUE', () => {
    test('для 1000 (MIN_VALUE * CUBE_TO_CM = 1000) должен вернуть 0.001 (MIN_VALUE)', () => {
      expect(formatVolume(1000)).toBe('0.001');
    });

    test('для 999 (меньше MIN_VALUE * CUBE_TO_CM) должен вернуть 0.001', () => {
      expect(formatVolume(999)).toBe('0.001');
    });
  });

  describe('отрицательные значения', () => {
    test('для -1000 должен вернуть 0.001 (Math.max с MIN_VALUE)', () => {
      expect(formatVolume(-1000)).toBe('0.001');
    });

    test('для -1000000 должен вернуть 0.001', () => {
      expect(formatVolume(-1000000)).toBe('0.001');
    });
  });
});

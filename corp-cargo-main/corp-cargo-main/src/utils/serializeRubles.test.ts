/* eslint-disable @typescript-eslint/no-explicit-any */
import { serializeRubles } from './serializeRubles';

describe('serializeRubles', () => {
  describe('конвертация и форматирование копеек в рубли', () => {
    test('должен конвертировать и форматировать целое число копеек', () => {
      expect(serializeRubles(100)).toBe('1,00 ₽');
      expect(serializeRubles(200)).toBe('2,00 ₽');
      expect(serializeRubles(1000)).toBe('10,00 ₽');
      expect(serializeRubles(5000)).toBe('50,00 ₽');
    });

    test('должен конвертировать и форматировать число с копейками', () => {
      expect(serializeRubles(150)).toBe('1,50 ₽');
      expect(serializeRubles(275)).toBe('2,75 ₽');
      expect(serializeRubles(999)).toBe('9,99 ₽');
    });

    test('должен конвертировать и форматировать отрицательное число', () => {
      expect(serializeRubles(-500)).toBe('-5,00 ₽');
      expect(serializeRubles(-1234.56)).toBe('-12,35 ₽');
    });

    test('должен форматировать ноль как рубли', () => {
      expect(serializeRubles(0)).toBe('0,00 ₽');
    });

    test('должен работать с большими числами', () => {
      expect(serializeRubles(1000000)).toBe('10 000,00 ₽');
      expect(serializeRubles(123456789)).toBe('1 234 567,89 ₽');
    });

    test('должен работать с дробными копейками', () => {
      expect(serializeRubles(100.5)).toBe('1,01 ₽');
      expect(serializeRubles(200.75)).toBe('2,01 ₽');
    });
  });

  describe('обработка null/undefined', () => {
    test('должен возвращать "-" для undefined', () => {
      expect(serializeRubles(undefined)).toBe('-');
    });

    test('должен возвращать "-" для null', () => {
      expect(serializeRubles(null as unknown as any)).toBe('-');
    });
  });

  describe('форматирование с копейками', () => {
    test('должен округлять копейки до двух знаков', () => {
      expect(serializeRubles(123.456)).toBe('1,23 ₽');
    });

    test('должен форматировать минимальное значение', () => {
      expect(serializeRubles(1)).toBe('0,01 ₽');
    });
  });
});

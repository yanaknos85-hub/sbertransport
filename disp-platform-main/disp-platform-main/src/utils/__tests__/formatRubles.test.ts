import { formatRubles } from '../formatRubles';

describe('formatRubles', () => {
  test('should format positive integer amount', () => {
    expect(formatRubles(100)).toBe('100,00 ₽');
    expect(formatRubles(1000)).toBe('1 000,00 ₽');
    expect(formatRubles(1000000)).toBe('1 000 000,00 ₽');
  });

  test('should format negative amount', () => {
    expect(formatRubles(-100)).toBe('-100,00 ₽');
    expect(formatRubles(-500.50)).toBe('-500,50 ₽');
  });

  test('should format zero amount', () => {
    expect(formatRubles(0)).toBe('0,00 ₽');
  });

  test('should format decimal amounts', () => {
    expect(formatRubles(100.50)).toBe('100,50 ₽');
    expect(formatRubles(100.5)).toBe('100,50 ₽');
    expect(formatRubles(123.456)).toBe('123,46 ₽');
    expect(formatRubles(0.99)).toBe('0,99 ₽');
  });

  test('should format amount with custom emptyValue', () => {
    expect(formatRubles(undefined, 'N/A')).toBe('N/A');
    expect(formatRubles(undefined, '')).toBe('');
    expect(formatRubles(undefined, '0')).toBe('0');
  });

  test('should handle undefined input with default emptyValue', () => {
    expect(formatRubles(undefined)).toBe('-');
  });

  test('should handle Infinity', () => {
    expect(formatRubles(Infinity)).toBe('-');
    expect(formatRubles(-Infinity)).toBe('-');
  });

  test('should handle NaN', () => {
    expect(formatRubles(NaN)).toBe('-');
  });

  test('should handle very large numbers', () => {
    expect(formatRubles(999999999)).toBe('999 999 999,00 ₽');
  });

  test('should handle very small decimal numbers', () => {
    expect(formatRubles(0.01)).toBe('0,01 ₽');
    expect(formatRubles(0.001)).toBe('0,00 ₽');
  });
});

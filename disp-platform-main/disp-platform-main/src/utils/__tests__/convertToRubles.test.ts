import { convertToRubles } from '../convertToRubles';

describe('convertToRubles', () => {
  test('should convert positive integer to rubles', () => {
    expect(convertToRubles(100)).toBe(1);
    expect(convertToRubles(500)).toBe(5);
    expect(convertToRubles(1000)).toBe(10);
    expect(convertToRubles(5000)).toBe(50);
    expect(convertToRubles(10000)).toBe(100);
  });

  test('should convert kopeks with decimal part', () => {
    expect(convertToRubles(150)).toBe(1.5);
    expect(convertToRubles(250)).toBe(2.5);
    expect(convertToRubles(750)).toBe(7.5);
    expect(convertToRubles(999)).toBe(9.99);
    expect(convertToRubles(101)).toBe(1.01);
  });

  test('should handle zero', () => {
    expect(convertToRubles(0)).toBe(0);
  });

  test('should handle small values less than 100', () => {
    expect(convertToRubles(1)).toBe(0.01);
    expect(convertToRubles(10)).toBe(0.1);
    expect(convertToRubles(50)).toBe(0.5);
    expect(convertToRubles(99)).toBe(0.99);
  });

  test('should handle large numbers', () => {
    expect(convertToRubles(100000)).toBe(1000);
    expect(convertToRubles(1000000)).toBe(10000);
    expect(convertToRubles(999999)).toBe(9999.99);
  });

  test('should handle floating point kopeks', () => {
    expect(convertToRubles(100.5)).toBe(1.005);
    expect(convertToRubles(123.45)).toBe(1.2345);
  });

  test('should handle negative values (debt)', () => {
    expect(convertToRubles(-100)).toBe(-1);
    expect(convertToRubles(-500)).toBe(-5);
    expect(convertToRubles(-150)).toBe(-1.5);
  });
});

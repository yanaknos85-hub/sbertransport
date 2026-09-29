import { formatDriverRating } from '../formatDriverRating';

describe('formatDriverRating', () => {
  test('should format valid ratings to two decimal places', () => {
    expect(formatDriverRating(100)).toBe('1.00');
    expect(formatDriverRating(50)).toBe('0.50');
    expect(formatDriverRating(75)).toBe('0.75');
    expect(formatDriverRating(25)).toBe('0.25');
    expect(formatDriverRating(1)).toBe('0.01');
    expect(formatDriverRating(99)).toBe('0.99');
  });

  test('should format ratings with one decimal place', () => {
    expect(formatDriverRating(10)).toBe('0.10');
    expect(formatDriverRating(20)).toBe('0.20');
    expect(formatDriverRating(30)).toBe('0.30');
    expect(formatDriverRating(80)).toBe('0.80');
    expect(formatDriverRating(90)).toBe('0.90');
  });

  test('should handle undefined rating with default empty value', () => {
    expect(formatDriverRating(undefined)).toBe('-');
  });

  test('should handle null rating with default empty value', () => {
    expect(formatDriverRating(null)).toBe('-');
  });

  test('should use custom empty value', () => {
    expect(formatDriverRating(undefined, 'N/A')).toBe('N/A');
    expect(formatDriverRating(null, '-')).toBe('-');
    expect(formatDriverRating(undefined, '')).toBe('');
    expect(formatDriverRating(undefined, 'No rating')).toBe('No rating');
  });

  test('should handle zero rating as empty value (0 is falsy)', () => {
    // 0 is falsy in JavaScript, so it returns emptyValue
    expect(formatDriverRating(0)).toBe('-');
    expect(formatDriverRating(0, 'N/A')).toBe('N/A');
  });

  test('should handle large ratings', () => {
    expect(formatDriverRating(150)).toBe('1.50');
    expect(formatDriverRating(200)).toBe('2.00');
    expect(formatDriverRating(1000)).toBe('10.00');
  });

  test('should handle floating point ratings', () => {
    expect(formatDriverRating(50.5)).toBe('0.51');
    expect(formatDriverRating(12.34)).toBe('0.12');
    expect(formatDriverRating(99.99)).toBe('1.00');
  });

  test('should handle negative ratings', () => {
    expect(formatDriverRating(-10)).toBe('-0.10');
    expect(formatDriverRating(-100)).toBe('-1.00');
  });

  test('should handle edge case of 100', () => {
    expect(formatDriverRating(100)).toBe('1.00');
  });
});

import { formatPhoneNumber } from '../formatPhoneNumber';

describe('formatPhoneNumber', () => {
  test('should format valid phone number with +7 prefix', () => {
    expect(formatPhoneNumber('+79123456789')).toBe('+7 (912) 345-67-89');
  });

  test('should format valid phone number with 7 prefix without +', () => {
    expect(formatPhoneNumber('79123456789')).toBe('+7 (912) 345-67-89');
  });

  test('should format phone number with 8 prefix (treated as 11 digits starting with 8)', () => {
    expect(formatPhoneNumber('89123456789')).toBe('-');
  });

  test('should format phone number with spaces', () => {
    expect(formatPhoneNumber('+7 912 345 67 89')).toBe('+7 (912) 345-67-89');
  });

  test('should format phone number with dashes', () => {
    expect(formatPhoneNumber('+7-912-345-67-89')).toBe('+7 (912) 345-67-89');
  });

  test('should format phone number with parentheses', () => {
    expect(formatPhoneNumber('+7 (912) 345-67-89')).toBe('+7 (912) 345-67-89');
  });

  test('should format phone number with mixed symbols', () => {
    expect(formatPhoneNumber('+7 (912) 345 67 89')).toBe('+7 (912) 345-67-89');
  });

  test('should handle undefined input', () => {
    expect(formatPhoneNumber(undefined)).toBe('-');
  });

  test('should handle empty string', () => {
    expect(formatPhoneNumber('')).toBe('-');
  });

  test('should handle short phone number (less than 10 digits)', () => {
    expect(formatPhoneNumber('123456789')).toBe('-');
  });

  test('should handle long phone number (more than 11 digits)', () => {
    expect(formatPhoneNumber('12345678901234567890')).toBe('-');
  });

  test('should format 10 digit number without country code', () => {
    expect(formatPhoneNumber('9123456789')).toBe('(912) 345-67-89');
  });

  test('should format phone number with non-numeric characters', () => {
    expect(formatPhoneNumber('abc+7 (912) 345-67-89xyz')).toBe('+7 (912) 345-67-89');
  });

  test('should handle phone number with extra leading zeros', () => {
    expect(formatPhoneNumber('0079123456789')).toBe('-');
  });

  test('should return dash for non-phone string', () => {
    expect(formatPhoneNumber('hello')).toBe('-');
  });

  test('should return dash for whitespace only', () => {
    expect(formatPhoneNumber('   ')).toBe('-');
  });

  test('should handle single digit input', () => {
    expect(formatPhoneNumber('5')).toBe('-');
  });

  test('should handle exactly 10 digits without country code', () => {
    expect(formatPhoneNumber('9123456789')).toBe('(912) 345-67-89');
  });

  test('should handle exactly 11 digits starting with 7', () => {
    expect(formatPhoneNumber('79123456789')).toBe('+7 (912) 345-67-89');
  });

  test('should handle exactly 11 digits starting with 8', () => {
    expect(formatPhoneNumber('89123456789')).toBe('-');
  });

  test('should handle 12 digits starting with 7', () => {
    expect(formatPhoneNumber('791234567890')).toBe('-');
  });

  test('should preserve formatting for already correct format', () => {
    expect(formatPhoneNumber('+7 (912) 345-67-89')).toBe('+7 (912) 345-67-89');
  });

  test('should handle very long non-phone string', () => {
    const veryLongString = 'a'.repeat(100);
    expect(formatPhoneNumber(veryLongString)).toBe('-');
  });

  test('should handle only country code', () => {
    expect(formatPhoneNumber('+7')).toBe('-');
  });

  test('should handle 7 followed by only 9 digits (10 total)', () => {
    expect(formatPhoneNumber('7123456789')).toBe('(712) 345-67-89');
  });

  test('should handle 7 followed by 10 digits (11 total)', () => {
    expect(formatPhoneNumber('71234567890')).toBe('+7 (123) 456-78-90');
  });

  test('should format number with dots as separators', () => {
    expect(formatPhoneNumber('+7.912.345-67-89')).toBe('+7 (912) 345-67-89');
  });

  test('should handle multiple plus signs', () => {
    expect(formatPhoneNumber('++79123456789')).toBe('+7 (912) 345-67-89');
  });

  test('should handle string with only digits 11 characters starting with 7', () => {
    expect(formatPhoneNumber('71234567890')).toBe('+7 (123) 456-78-90');
  });
});

import { formatPhoneNumber } from './formatPhoneNumber';

describe('formatPhoneNumber', () => {
  test('для строки "89001234567" должен вернуть "+7 (900) 123-45-67"', () => {
    expect(formatPhoneNumber('89001234567')).toBe('+7 (900) 123-45-67');
  });

  test('для строки "79001234567" должен вернуть "+7 (900) 123-45-67"', () => {
    expect(formatPhoneNumber('79001234567')).toBe('+7 (900) 123-45-67');
  });

  test('для строки "9001234567" (без кода страны) должен вернуть "(900) 123-45-67"', () => {
    expect(formatPhoneNumber('9001234567')).toBe('(900) 123-45-67');
  });

  test('для строки "+79001234567" должен вернуть "+7 (900) 123-45-67"', () => {
    expect(formatPhoneNumber('+79001234567')).toBe('+7 (900) 123-45-67');
  });

  test('для строки "8 (900) 123-45-67" должен вернуть "+7 (900) 123-45-67"', () => {
    expect(formatPhoneNumber('8 (900) 123-45-67')).toBe('+7 (900) 123-45-67');
  });

  test('для строки "900-123-45-67" (без кода страны) должен вернуть "(900) 123-45-67"', () => {
    expect(formatPhoneNumber('900-123-45-67')).toBe('(900) 123-45-67');
  });

  test('для строки "900 123 45 67" (без кода страны) должен вернуть "(900) 123-45-67"', () => {
    expect(formatPhoneNumber('900 123 45 67')).toBe('(900) 123-45-67');
  });

  test('для null должен вернуть "-"', () => {
    expect(formatPhoneNumber(null)).toBe('-');
  });

  test('для undefined должен вернуть "-"', () => {
    expect(formatPhoneNumber(undefined)).toBe('-');
  });

  test('для пустой строки должен вернуть "-"', () => {
    expect(formatPhoneNumber('')).toBe('-');
  });

  test('для строки "123" (некорректная длина) должен вернуть "-"', () => {
    expect(formatPhoneNumber('123')).toBe('-');
  });

  test('для строки "abcdefg" (буквы) должен вернуть "-"', () => {
    expect(formatPhoneNumber('abcdefg')).toBe('-');
  });

  test('для строки "89001234567890" (слишком длинная) должен вернуть "-"', () => {
    expect(formatPhoneNumber('89001234567890')).toBe('-');
  });

  test('для строки "09001234567" (не 7 или 8 в начале) должен вернуть "-"', () => {
    expect(formatPhoneNumber('09001234567')).toBe('-');
  });
});

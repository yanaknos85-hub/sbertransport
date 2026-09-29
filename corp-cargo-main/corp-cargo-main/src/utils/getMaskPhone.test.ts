import { getMaskPhone } from './getMaskPhone';

describe('getMaskPhone', () => {
  test('для строки "+7 (900) 123-45-67" должен вернуть "+79001234567"', () => {
    expect(getMaskPhone('+7 (900) 123-45-67')).toBe('+79001234567');
  });

  test('для строки "8 (900) 123-45-67" должен вернуть "89001234567"', () => {
    expect(getMaskPhone('8 (900) 123-45-67')).toBe('89001234567');
  });

  test('для строки "900-123-45-67" должен вернуть "9001234567"', () => {
    expect(getMaskPhone('900-123-45-67')).toBe('9001234567');
  });

  test('для строки "900 123 45 67" должен вернуть "9001234567"', () => {
    expect(getMaskPhone('900 123 45 67')).toBe('9001234567');
  });

  test('для строки "(900) 123-45-67" должен вернуть "9001234567"', () => {
    expect(getMaskPhone('(900) 123-45-67')).toBe('9001234567');
  });

  test('для строки "8_900_123_45_67" с подчёркиваниями должен вернуть "89001234567"', () => {
    expect(getMaskPhone('8_900_123_45_67')).toBe('89001234567');
  });

  test('для строки "123-456-789" должен вернуть "123456789"', () => {
    expect(getMaskPhone('123-456-789')).toBe('123456789');
  });

  test('для строки "abcdef" (только буквы) должен вернуть "abcdef"', () => {
    expect(getMaskPhone('abcdef')).toBe('abcdef');
  });

  test('для строки "123 abc def-456" (смешанная) должен вернуть "123abcdef456"', () => {
    expect(getMaskPhone('123 abc def-456')).toBe('123abcdef456');
  });

  test('для пустой строки должен вернуть undefined', () => {
    expect(getMaskPhone('')).toBeUndefined();
  });

  test('для undefined должен вернуть undefined', () => {
    expect(getMaskPhone(undefined)).toBeUndefined();
  });

  test('для null должен вернуть undefined', () => {
    // TypeScript тип позволяет string | undefined, но в JS null тоже может прийти
    // Проверим поведение — функция не обрабатывает null отдельно, вернёт undefined
    expect(getMaskPhone(null as unknown as string | undefined)).toBeUndefined();
  });
});

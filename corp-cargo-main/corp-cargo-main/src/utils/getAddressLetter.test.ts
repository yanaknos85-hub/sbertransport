import { getAddressLetter } from './getAddressLetter';

describe('getAddressLetter', () => {
  it('should return "A" for index 0', () => {
    expect(getAddressLetter(0)).toBe('A');
  });

  it('should return "B" for index 1', () => {
    expect(getAddressLetter(1)).toBe('B');
  });

  it('should return "C" for index 2', () => {
    expect(getAddressLetter(2)).toBe('C');
  });

  it('should return "Z" for index 25', () => {
    expect(getAddressLetter(25)).toBe('Z');
  });

  it('should handle negative indices', () => {
    // Для index = -1: 97 + (-1) = 96, символ с кодом 96 - это '`'
    expect(getAddressLetter(-1)).toBe('`');
  });

  it('should handle indices beyond 25', () => {
    // Для index = 26: 97 + 26 = 123, символ с кодом 123 - это '{'
    expect(getAddressLetter(26)).toBe('{');

    // Для index = 27: 97 + 27 = 124, символ с кодом 124 - это '|'
    expect(getAddressLetter(27)).toBe('|');
  });

  it('should always return uppercase letters within valid range (0-25)', () => {
    for (let i = 0; i <= 25; i++) {
      const result = getAddressLetter(i);
      expect(result).toBe(String.fromCharCode(65 + i)); // 65 - код 'A'
      expect(result).toMatch(/^[A-Z]$/);
    }
  });

  it('should return ASCII characters based on index calculation', () => {
    // Проверка на конкретных значениях
    expect(getAddressLetter(0)).toBe('A');  // 97 + 0 = 97 'a' -> uppercase 'A'
    expect(getAddressLetter(13)).toBe('N'); // 97 + 13 = 110 'n' -> 'N'
    expect(getAddressLetter(25)).toBe('Z'); // 97 + 25 = 122 'z' -> 'Z'
  });
});

import { makeFirstFoundCharUppercase } from '../calendar';

describe('makeFirstFoundCharUppercase', () => {
  test('should capitalize the first found Latin letter', () => {
    expect(makeFirstFoundCharUppercase('hello')).toBe('Hello');
    expect(makeFirstFoundCharUppercase('world')).toBe('World');
    expect(makeFirstFoundCharUppercase('abc123')).toBe('Abc123');
  });

  test('should capitalize the first found Cyrillic letter', () => {
    expect(makeFirstFoundCharUppercase('привет')).toBe('Привет');
    expect(makeFirstFoundCharUppercase('мир')).toBe('Мир');
    expect(makeFirstFoundCharUppercase('тест123')).toBe('Тест123');
  });

  test('should handle strings with leading non-letter characters', () => {
    expect(makeFirstFoundCharUppercase('123hello')).toBe('123Hello');
    expect(makeFirstFoundCharUppercase('   test')).toBe('   Test');
    expect(makeFirstFoundCharUppercase('...go')).toBe('...Go');
  });

  test('should return the original string if no letters are found', () => {
    expect(makeFirstFoundCharUppercase('12345')).toBe('12345');
    expect(makeFirstFoundCharUppercase('   ')).toBe('   ');
    expect(makeFirstFoundCharUppercase('')).toBe('');
  });

  test('should handle strings where the first letter is already uppercase', () => {
    expect(makeFirstFoundCharUppercase('Hello')).toBe('Hello');
    expect(makeFirstFoundCharUppercase('Привет')).toBe('Привет');
  });

  test('should capitalize the first letter even if it is not at the start', () => {
    expect(makeFirstFoundCharUppercase('123abc')).toBe('123Abc');
    expect(makeFirstFoundCharUppercase('...привет')).toBe('...Привет');
  });
});

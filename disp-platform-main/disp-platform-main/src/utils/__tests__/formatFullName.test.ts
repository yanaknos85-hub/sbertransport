import { formatFullName } from '../formatFullName';

describe('formatFullName', () => {
  test('should format full name with all parts', () => {
    expect(formatFullName('Иван', 'Петров', 'Сидорович')).toBe('Петров И.С.');
    expect(formatFullName('Мария', 'Смирнова', 'Алексеевна')).toBe('Смирнова М.А.');
    expect(formatFullName('Алексей', 'Кузнецов', 'Михайлович')).toBe('Кузнецов А.М.');
  });

  test('should format name with first and last name only', () => {
    expect(formatFullName('Иван', 'Петров', undefined)).toBe('Петров И.');
    expect(formatFullName('Мария', 'Смирнова', undefined)).toBe('Смирнова М.');
    expect(formatFullName('Алексей', 'Кузнецов', '')).toBe('Кузнецов А.');
  });

  test('should format name with last name only', () => {
    expect(formatFullName(undefined, 'Петров', undefined)).toBe('Петров');
    expect(formatFullName(undefined, 'Смирнова', undefined)).toBe('Смирнова');
    expect(formatFullName('', 'Кузнецов', '')).toBe('Кузнецов');
  });

  test('should handle empty strings', () => {
    expect(formatFullName('', '', '')).toBe('');
    expect(formatFullName(' ', ' ', ' ')).toBe('');
  });

  test('should handle single character names', () => {
    expect(formatFullName('А', 'Б', 'В')).toBe('Б А.В.');
    expect(formatFullName('X', 'Y', 'Z')).toBe('Y X.Z.');
  });

  test('should handle long names', () => {
    expect(formatFullName('Александр', 'Владимирович', 'Сергеевич')).toBe('Владимирович А.С.');
    expect(formatFullName('Николай', 'Иванов', 'Петрович')).toBe('Иванов Н.П.');
  });

  test('should handle names with special characters', () => {
    expect(formatFullName('Жан-Клод', 'Ван-Дамм', 'Пьер').trim()).toBe('Ван-Дамм Ж.П.');
  });

  test('should handle Cyrillic names with apostrophes', () => {
    expect(formatFullName('О\'Хара', 'О\'Нил', 'Д\'Арк')).toBe('О\'Нил О.Д.');
  });

  test('should preserve whitespace in names', () => {
    expect(formatFullName('  ', '  ', '  ')).toBe('');
  });

  test('should handle case correctly', () => {
    expect(formatFullName('ИВАН', 'ПЕТРОВ', 'СИДОРОВИЧ')).toBe('ПЕТРОВ И.С.');
    expect(formatFullName('иван', 'петров', 'сидорович')).toBe('петров и.с.');
    expect(formatFullName('Иван', 'Петров', 'Сидорович')).toBe('Петров И.С.');
  });

  test('should handle names with numbers', () => {
    expect(formatFullName('Иван2', 'Петров3', 'Сидорович4')).toBe('Петров3 И.С.');
  });

  test('should handle very long names', () => {
    const longFirstName = 'Александр' + 'Александр'.repeat(10);
    const longLastName = 'Петров' + 'Петров'.repeat(10);
    const longMiddleName = 'Сидорович' + 'Сидорович'.repeat(10);

    const result = formatFullName(longFirstName, longLastName, longMiddleName);
    expect(result).toBe(`Петров${'Петров'.repeat(10)} А.С.`);
  });
});

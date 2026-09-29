import { formatName, upFirst } from '../formatName';

describe('formatName', () => {
  it('should format full name with all parts', () => {
    const employee = {
      lastName: 'Иванов',
      firstName: 'Иван',
      patronymic: 'Иванович',
    };

    expect(formatName(employee)).toBe('Иванов Иван Иванович');
  });

  it('should format name without patronymic', () => {
    const employee = {
      lastName: 'Петров',
      firstName: 'Петр',
      patronymic: null,
    };

    expect(formatName(employee)).toBe('Петров Петр');
  });

  it('should format name with undefined patronymic', () => {
    const employee = {
      lastName: 'Сидоров',
      firstName: 'Сидор',
    };

    expect(formatName(employee)).toBe('Сидоров Сидор');
  });

  it('should format name with only last name', () => {
    const employee = {
      lastName: 'Смирнов',
    };

    expect(formatName(employee)).toBe('Смирнов');
  });

  it('should format name with only first name', () => {
    const employee = {
      firstName: 'Алексей',
    };

    expect(formatName(employee)).toBe('Алексей');
  });

  it('should handle empty object', () => {
    const employee = {};

    expect(formatName(employee)).toBe('');
  });

  it('should format name with empty strings', () => {
    const employee = {
      lastName: '',
      firstName: '',
      patronymic: '',
    };

    expect(formatName(employee)).toBe('');
  });

  it('should format name with spaces', () => {
    const employee = {
      lastName: '  Иванов  ',
      firstName: '  Иван  ',
      patronymic: '  Иванович  ',
    };

    expect(formatName(employee)).toBe('  Иванов     Иван     Иванович  ');
  });
});

describe('upFirst', () => {
  it('should capitalize first letter of a string', () => {
    expect(upFirst('иванов')).toBe('Иванов');
    expect(upFirst('петров')).toBe('Петров');
    expect(upFirst('hello')).toBe('Hello');
  });

  it('should handle empty string', () => {
    expect(upFirst('')).toBe('');
  });

  it('should handle single character', () => {
    expect(upFirst('а')).toBe('А');
    expect(upFirst('b')).toBe('B');
  });

  it('should not change already capitalized string', () => {
    expect(upFirst('Иванов')).toBe('Иванов');
    expect(upFirst('Hello')).toBe('Hello');
  });

  it('should only change first character', () => {
    expect(upFirst('иВАНОВ')).toBe('ИВАНОВ');
    expect(upFirst('hello WORLD')).toBe('Hello WORLD');
  });

  it('should handle string with spaces', () => {
    expect(upFirst(' иванов')).toBe(' иванов');
    expect(upFirst('иванов иванович')).toBe('Иванов иванович');
  });

  it('should handle special characters', () => {
    expect(upFirst('123abc')).toBe('123abc');
    expect(upFirst('#test')).toBe('#test');
  });

  it('should handle undefined input', () => {
    expect(upFirst(undefined)).toBe(undefined);
  });

  it('should handle null input', () => {
    expect(upFirst(null)).toBe(null);
  });
});

describe('Integration tests', () => {
  it('should format and capitalize name', () => {
    const employee = {
      lastName: 'иванов',
      firstName: 'иван',
      patronymic: 'иванович',
    };

    const formatted = formatName(employee);
    const capitalized = formatted.split(' ').map(upFirst).join(' ');

    expect(capitalized).toBe('Иванов Иван Иванович');
  });

  it('should handle partial data with capitalization', () => {
    const employee = {
      lastName: 'петров',
      firstName: 'петр',
    };

    const formatted = formatName(employee);
    const capitalized = formatted.split(' ').map(upFirst).join(' ');

    expect(capitalized).toBe('Петров Петр');
  });
});

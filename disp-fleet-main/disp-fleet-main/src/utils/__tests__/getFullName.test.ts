import { getFullName } from '../getFullName';

interface ExtendedHuman {
  firstName?: string | null;
  lastName?: string | null;
  patronymic?: string | null;
  customField?: string;
}

describe('getFullName', () => {
  test('should return full name with all fields', () => {
    const human = {
      lastName: 'Иванов',
      firstName: 'Иван',
      patronymic: 'Иванович',
    };
    expect(getFullName(human)).toBe('Иванов Иван Иванович');
  });

  test('should return name with first and last name only', () => {
    const human = {
      lastName: 'Иванов',
      firstName: 'Иван',
      patronymic: null,
    };
    expect(getFullName(human)).toBe('Иванов Иван');
  });

  test('should return name with last name only', () => {
    const human = {
      lastName: 'Иванов',
      firstName: null,
      patronymic: null,
    };
    expect(getFullName(human)).toBe('Иванов');
  });

  test('should return name with first name only', () => {
    const human = {
      lastName: null,
      firstName: 'Иван',
      patronymic: null,
    };
    expect(getFullName(human)).toBe('Иван');
  });

  test('should return name with last and patronymic only', () => {
    const human = {
      lastName: 'Иванов',
      firstName: null,
      patronymic: 'Иванович',
    };
    expect(getFullName(human)).toBe('Иванов Иванович');
  });

  test('should handle empty strings', () => {
    const human = {
      lastName: '',
      firstName: '',
      patronymic: '',
    };
    expect(getFullName(human)).toBe('');
  });

  test('should handle undefined values', () => {
    const human = {
      lastName: undefined,
      firstName: undefined,
      patronymic: undefined,
    };
    expect(getFullName(human)).toBe('');
  });

  test('should handle null values', () => {
    const human = {
      lastName: null,
      firstName: null,
      patronymic: null,
    };
    expect(getFullName(human)).toBe('');
  });

  test('should handle mixed null and undefined values', () => {
    const human = {
      lastName: null,
      firstName: undefined,
      patronymic: 'Иванович',
    };
    expect(getFullName(human)).toBe('Иванович');
  });

  test('should handle whitespace-only strings', () => {
    const human = {
      lastName: '   ',
      firstName: '   ',
      patronymic: '   ',
    };
    expect(getFullName(human)).toBe('           ');
  });

  test('should handle single character names', () => {
    const human = {
      lastName: 'И',
      firstName: 'П',
      patronymic: 'С',
    };
    expect(getFullName(human)).toBe('И П С');
  });

  test('should handle names with special characters', () => {
    const human = {
      lastName: 'Ван-Дамм',
      firstName: 'Жан-Клод',
      patronymic: 'Пьер-Сimon',
    };
    expect(getFullName(human)).toBe('Ван-Дамм Жан-Клод Пьер-Сimon');
  });

  test('should handle Cyrillic names with apostrophes', () => {
    const human = {
      lastName: 'О\'Нил',
      firstName: 'О\'Хара',
      patronymic: 'Д\'Арк',
    };
    expect(getFullName(human)).toBe('О\'Нил О\'Хара Д\'Арк');
  });

  test('should handle case correctly', () => {
    const human = {
      lastName: 'ПЕТРОВ',
      firstName: 'ИВАН',
      patronymic: 'СИДОРОВИЧ',
    };
    expect(getFullName(human)).toBe('ПЕТРОВ ИВАН СИДОРОВИЧ');
  });

  test('should handle names with numbers', () => {
    const human = {
      lastName: 'Петров3',
      firstName: 'Иван2',
      patronymic: 'Сидорович4',
    };
    expect(getFullName(human)).toBe('Петров3 Иван2 Сидорович4');
  });

  test('should handle empty object', () => {
    expect(getFullName({})).toBe('');
  });

  test('should handle object with only some fields', () => {
    const human = {
      firstName: 'Иван',
    };
    expect(getFullName(human)).toBe('Иван');
  });

  test('should handle object with custom fields extended from Human', () => {
    const extendedHuman: ExtendedHuman = {
      lastName: 'Иванов',
      firstName: 'Иван',
      patronymic: 'Иванович',
      customField: 'some value',
    };
    expect(getFullName(extendedHuman)).toBe('Иванов Иван Иванович');
  });

  test('should handle extended object with only required fields', () => {
    const extendedHuman: ExtendedHuman = {
      firstName: 'Иван',
      customField: 'some value',
    };
    expect(getFullName(extendedHuman)).toBe('Иван');
  });

  test('should handle very long names', () => {
    const longName = 'Александр'.repeat(10);
    const human = {
      lastName: longName,
      firstName: longName,
      patronymic: longName,
    };
    expect(getFullName(human)).toBe(`${longName} ${longName} ${longName}`);
  });

  test('should preserve spaces within names', () => {
    const human = {
      lastName: 'Иванов Иванов',
      firstName: 'Иван Иван',
      patronymic: 'Иванович Иванович',
    };
    expect(getFullName(human)).toBe('Иванов Иванов Иван Иван Иванович Иванович');
  });

  test('should handle names with hyphens', () => {
    const human = {
      lastName: 'Иванов-Сидоров',
      firstName: 'Петр-Алексей',
      patronymic: 'Семёнович-Петрович',
    };
    expect(getFullName(human)).toBe('Иванов-Сидоров Петр-Алексей Семёнович-Петрович');
  });

  test('should handle names with multiple spaces', () => {
    const human = {
      lastName: 'Иванов  Иванов',
      firstName: 'Иван  Иван',
      patronymic: 'Иванович  Иванович',
    };
    expect(getFullName(human)).toBe('Иванов  Иванов Иван  Иван Иванович  Иванович');
  });

  test('should handle null firstName with valid lastName and patronymic', () => {
    const human = {
      lastName: 'Иванов',
      firstName: null,
      patronymic: 'Иванович',
    };
    expect(getFullName(human)).toBe('Иванов Иванович');
  });

  test('should handle undefined patronymic with valid lastName and firstName', () => {
    const human = {
      lastName: 'Иванов',
      firstName: 'Иван',
      patronymic: undefined,
    };
    expect(getFullName(human)).toBe('Иванов Иван');
  });

  test('should handle empty string lastName with valid firstName and patronymic', () => {
    const human = {
      lastName: '',
      firstName: 'Иван',
      patronymic: 'Иванович',
    };
    expect(getFullName(human)).toBe('Иван Иванович');
  });
});

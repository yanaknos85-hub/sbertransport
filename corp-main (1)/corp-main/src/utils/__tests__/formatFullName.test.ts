import { formatFullName } from '../formatFullName';

describe('formatFullName', () => {
  it('формирует полное ФИО в формате "Фамилия Имя Отчество"', () => {
    expect(formatFullName('Иван', 'Иванов', 'Иванович')).toBe('Иванов Иван Иванович');
  });

  it('возвращает только фамилию с двумя пробелами, если задано только lastName', () => {
    expect(formatFullName(undefined, 'Иванов', undefined)).toBe('Иванов  ');
  });

  it('возвращает два пробела для трёх undefined (между пустыми частями сохраняются разделители)', () => {
    expect(formatFullName(undefined, undefined, undefined)).toBe('  ');
  });

  it('возвращает два пробела для трёх пустых строк', () => {
    expect(formatFullName('', '', '')).toBe('  ');
  });

  it('подставляет пустую строку вместо undefined', () => {
    expect(formatFullName(undefined, 'Петров', 'Сергеевич')).toBe('Петров  Сергеевич');
  });

  it('подставляет пустую строку вместо пустой строки', () => {
    expect(formatFullName('', 'Сидоров', '')).toBe('Сидоров  ');
  });

  it('собирает порядок как "Фамилия Имя Отчество" независимо от регистра', () => {
    expect(formatFullName('анна', 'ПЕТРОВА', 'Сергеевна')).toBe('ПЕТРОВА анна Сергеевна');
  });

  it('поддерживает составные имена с пробелами', () => {
    expect(formatFullName('Анна-Мария', 'Иванова', 'Сергеевна')).toBe('Иванова Анна-Мария Сергеевна');
  });
});

import { formatFullName } from './formatFullName';

describe('formatFullName', () => {
  test('возвращает полное имя с инициалами', () => {
    expect(formatFullName('Иван', 'Иванов', 'Иванович')).toBe('Иванов И.И.');
  });

  test('работает только с именем и фамилией', () => {
    expect(formatFullName('Иван', 'Иванов', undefined)).toBe('Иванов И.');
    expect(formatFullName('Иван', 'Иванов', '')).toBe('Иванов И.');
  });

  test('работает только с фамилией', () => {
    expect(formatFullName(undefined, 'Иванов', undefined)).toBe('Иванов ');
    expect(formatFullName('', 'Иванов', '')).toBe('Иванов ');
  });

  test('работает с пустым вводом', () => {
    expect(formatFullName(undefined, undefined, undefined)).toBe(' ');
    expect(formatFullName('', '', '')).toBe(' ');
  });

  test('работает только с именем (без фамилии)', () => {
    expect(formatFullName('Иван', undefined, undefined)).toBe(' И.');
    expect(formatFullName('Иван', '', '')).toBe(' И.');
  });

  test('работает с именем и отчеством (без фамилии)', () => {
    expect(formatFullName('Иван', undefined, 'Иванович')).toBe(' И.И.');
    expect(formatFullName('Иван', '', 'Иванович')).toBe(' И.И.');
  });

  test('обрезает имя и отчество до одной буквы', () => {
    expect(formatFullName('Иван', 'Иванов', 'Иванович')).toBe('Иванов И.И.');
    expect(formatFullName('Александр', 'Петров', 'Сергеевич')).toBe('Петров А.С.');
  });

  test('работает с разным регистром', () => {
    expect(formatFullName('иван', 'ИВАНОВ', 'Иванович')).toBe('ИВАНОВ и.И.');
  });

  test('работает с однобуквенными именами', () => {
    expect(formatFullName('И', 'Иванов', 'И')).toBe('Иванов И.И.');
  });
});

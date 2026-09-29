import { formatPassengerName } from './formatPassengerName';

describe('formatPassengerName', () => {
  describe('null/undefined/пустые значения', () => {
    test('для undefined должен вернуть пустую строку', () => {
      expect(formatPassengerName(undefined)).toBe('');
    });
  });

  describe('отдельные поля', () => {
    test('должен возвращать только фамилию', () => {
      const result = formatPassengerName({
        lastName: 'Иванов',
        firstName: '',
        patronymic: '',
      });
      expect(result).toBe('Иванов');
    });

    test('должен возвращать только имя', () => {
      const result = formatPassengerName({
        lastName: '',
        firstName: 'Иван',
        patronymic: '',
      });
      expect(result).toBe('Иван');
    });

    test('должен возвращать только отчество', () => {
      const result = formatPassengerName({
        lastName: '',
        firstName: '',
        patronymic: 'Петрович',
      });
      expect(result).toBe('Петрович');
    });

    test('должен возвращать только фамилию для undefined имени и отчества', () => {
      const result = formatPassengerName({
        lastName: 'Иванов',
        firstName: undefined,
        patronymic: undefined,
      });
      expect(result).toBe('Иванов');
    });
  });

  describe('два поля', () => {
    test('должен формировать фамилию и имя', () => {
      const result = formatPassengerName({
        lastName: 'Иванов',
        firstName: 'Иван',
        patronymic: '',
      });
      expect(result).toBe('Иванов Иван');
    });

    test('должен формировать фамилию и отчество', () => {
      const result = formatPassengerName({
        lastName: 'Иванов',
        firstName: '',
        patronymic: 'Петрович',
      });
      expect(result).toBe('Иванов Петрович');
    });

    test('должен формировать имя и отчество', () => {
      const result = formatPassengerName({
        lastName: '',
        firstName: 'Иван',
        patronymic: 'Петрович',
      });
      expect(result).toBe('Иван Петрович');
    });

    test('должен формировать фамилию и имя для undefined отчества', () => {
      const result = formatPassengerName({
        lastName: 'Иванов',
        firstName: 'Иван',
        patronymic: undefined,
      });
      expect(result).toBe('Иванов Иван');
    });
  });

  describe('все три поля', () => {
    test('должен формировать полное ФИО', () => {
      const result = formatPassengerName({
        lastName: 'Иванов',
        firstName: 'Иван',
        patronymic: 'Петрович',
      });
      expect(result).toBe('Иванов Иван Петрович');
    });

    test('должен формировать ФИО с латинскими буквами', () => {
      const result = formatPassengerName({
        lastName: 'Ivanov',
        firstName: 'Ivan',
        patronymic: 'Petrovich',
      });
      expect(result).toBe('Ivanov Ivan Petrovich');
    });
  });

  describe('с пробелами и unicode', () => {
    test('должен сохранять пробелы в значениях', () => {
      const result = formatPassengerName({
        lastName: 'Иванов',
        firstName: 'Иван Петров',
        patronymic: 'Сидорович',
      });
      expect(result).toBe('Иванов Иван Петров Сидорович');
    });

    test('должен работать с unicode символами', () => {
      const result = formatPassengerName({
        lastName: 'Ян',
        firstName: 'У́нчжэ',
        patronymic: 'Минь',
      });
      expect(result).toBe('Ян У́нчжэ Минь');
    });
  });

  describe('с пустыми строками', () => {
    test('должен игнорировать пустые строки', () => {
      const result = formatPassengerName({
        lastName: '',
        firstName: '',
        patronymic: '',
      });
      expect(result).toBe('');
    });

    test('должен игнорировать undefined внутри объекта', () => {
      const result = formatPassengerName({
        lastName: undefined,
        firstName: undefined,
        patronymic: '',
      });
      expect(result).toBe('');
    });
  });
});

/* eslint-disable @typescript-eslint/no-explicit-any */
import { formatName, formatNameShort } from './formatName';

describe('formatName', () => {
  test('должен формировать полное ФИО', () => {
    const result = formatName({
      lastName: 'Иванов',
      firstName: 'Иван',
      patronymic: 'Петрович',
    });
    expect(result).toBe('Иванов Иван Петрович');
  });

  test('должен формировать ФИО с латинскими буквами', () => {
    const result = formatName({
      lastName: 'Ivanov',
      firstName: 'Ivan',
      patronymic: 'Petrovich',
    });
    expect(result).toBe('Ivanov Ivan Petrovich');
  });

  test('должен работать без отчества', () => {
    const result = formatName({
      lastName: 'Иванов',
      firstName: 'Иван',
      patronymic: '',
    });
    expect(result).toBe('Иванов Иван');
  });

  test('должен работать без имени', () => {
    const result = formatName({
      lastName: 'Иванов',
      firstName: '',
      patronymic: 'Петрович',
    });
    expect(result).toBe('Иванов Петрович');
  });

  test('должен работать без фамилии', () => {
    const result = formatName({
      lastName: '',
      firstName: 'Иван',
      patronymic: 'Петрович',
    });
    expect(result).toBe('Иван Петрович');
  });

  test('должен возвращать только фамилию', () => {
    const result = formatName({
      lastName: 'Иванов',
      firstName: '',
      patronymic: '',
    });
    expect(result).toBe('Иванов');
  });

  test('должен возвращать только имя', () => {
    const result = formatName({
      lastName: '',
      firstName: 'Иван',
      patronymic: '',
    });
    expect(result).toBe('Иван');
  });

  test('должен возвращать только отчество', () => {
    const result = formatName({
      lastName: '',
      firstName: '',
      patronymic: 'Петрович',
    });
    expect(result).toBe('Петрович');
  });

  test('должен возвращать пустую строку для пустых данных', () => {
    const result = formatName({
      lastName: '',
      firstName: '',
      patronymic: '',
    });
    expect(result).toBe('');
  });

  test('должен работать с пробелами в имени', () => {
    const result = formatName({
      lastName: 'Иванов',
      firstName: 'Иван Петров',
      patronymic: 'Сидорович',
    });
    expect(result).toBe('Иванов Иван Петров Сидорович');
  });

  test('должен работать с unicode символами', () => {
    const result = formatName({
      lastName: 'Ян',
      firstName: 'У́нчжэ',
      patronymic: 'Минь',
    });
    expect(result).toBe('Ян У́нчжэ Минь');
  });
});

describe('formatNameShort', () => {
  test('должен формировать ФИО с инициалами в прямом порядке', () => {
    const result = formatNameShort({
      lastName: 'Иванов',
      firstName: 'Иван',
      patronymic: 'Петрович',
    });
    expect(result).toBe('Иванов И. П.');
  });

  test('должен формировать ФИО с инициалами в обратном порядке', () => {
    const result = formatNameShort({
      lastName: 'Иванов',
      firstName: 'Иван',
      patronymic: 'Петрович',
    }, true);
    expect(result).toBe('И. П. Иванов');
  });

  test('должен работать без отчества в прямом порядке', () => {
    const result = formatNameShort({
      lastName: 'Иванов',
      firstName: 'Иван',
      patronymic: '',
    });
    expect(result).toBe('Иванов И.');
  });

  test('должен работать без отчества в обратном порядке', () => {
    const result = formatNameShort({
      lastName: 'Иванов',
      firstName: 'Иван',
      patronymic: '',
    }, true);
    expect(result).toBe('И. Иванов');
  });

  test('должен работать без имени и отчества', () => {
    const result = formatNameShort({
      lastName: 'Иванов',
      firstName: '',
      patronymic: '',
    });
    expect(result).toBe('Иванов');
  });

  test('должен возвращать только фамилию при пустых имени и отчестве', () => {
    const result = formatNameShort({
      lastName: 'Иванов',
      firstName: '',
      patronymic: '',
    });
    expect(result).toBe('Иванов');
  });

  test('должен обрабатывать однобуквенные имена', () => {
    const result = formatNameShort({
      lastName: 'В',
      firstName: 'А',
      patronymic: 'Б',
    });
    expect(result).toBe('В А. Б.');
  });

  test('должен работать с unicode символами', () => {
    const result = formatNameShort({
      lastName: 'Ян',
      firstName: 'У́нчжэ',
      patronymic: 'Минь',
    });
    expect(result).toBe('Ян У. М.');
  });

  test('должен работать с пустыми строками в прямом порядке', () => {
    const result = formatNameShort({
      lastName: '',
      firstName: '',
      patronymic: '',
    });
    expect(result).toBe('');
  });

  test('должен работать с пустыми строками в обратном порядке', () => {
    const result = formatNameShort({
      lastName: '',
      firstName: '',
      patronymic: '',
    }, true);
    expect(result).toBe('');
  });
});

describe('formatNameShort edge cases', () => {
  test('должен заглавлять инициалы', () => {
    const result = formatNameShort({
      lastName: 'иванов',
      firstName: 'иван',
      patronymic: 'Петрович',
    });
    expect(result).toBe('иванов И. П.');
  });

  test('должен работать с пробелами в имени', () => {
    const result = formatNameShort({
      lastName: 'Иванов',
      firstName: 'Иван Петров',
      patronymic: 'Сидорович',
    });
    expect(result).toBe('Иванов И. С.');
  });
});

describe('formatNameShort edge cases - undef/null', () => {
  test('должен возвращать пустую строку для undefined', () => {
    const result = formatNameShort(undefined as unknown as any);
    expect(result).toBe('');
  });

  test('должен возвращать пустую строку для null', () => {
    const result = formatNameShort(null as unknown as any);
    expect(result).toBe('');
  });
});

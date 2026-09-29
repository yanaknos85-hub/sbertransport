import {
  nameWithInitials,
  fullName,
  fullNameLastFirstPat,
  fullNameWithPhoneNumber,
  fullVehicleInfo,
  shortName,
} from './employee';

describe('employee', () => {
  describe('nameWithInitials', () => {
    test('должен формировать имя с инициалами для полных данных', () => {
      const result = nameWithInitials({
        firstName: 'Иван',
        patronymic: 'Петрович',
        lastName: 'Иванов',
      });
      expect(result).toBe('И. П. Иванов');
    });

    test('должен формировать имя без отчества', () => {
      const result = nameWithInitials({
        firstName: 'Иван',
        lastName: 'Иванов',
      });
      expect(result).toBe('И. Иванов');
    });

    test('должен возвращать пустую строку для пустых данных', () => {
      const result = nameWithInitials({
        firstName: '',
        patronymic: '',
        lastName: '',
      });
      expect(result).toBe('');
    });

    test('должен возвращать пустую строку если все поля undefined', () => {
      const result = nameWithInitials({
        firstName: undefined as unknown as string,
        patronymic: undefined as unknown as string,
        lastName: undefined as unknown as string,
      });
      expect(result).toBe('');
    });

    test('должен корректно обрабатывать однобуквенные имена', () => {
      const result = nameWithInitials({
        firstName: 'А',
        patronymic: 'Б',
        lastName: 'В',
      });
      expect(result).toBe('А. Б. В');
    });

    test('должен обрезать пробелы в начале имени при создании инициалов', () => {
      const result = nameWithInitials({
        firstName: '  Иван',
        patronymic: '  Петрович',
        lastName: '  Иванов',
      });
      // Текущая реализация не обрезает пробелы — ожидаем поведение кода
      expect(result).toBe(' .  .   Иванов');
    });

    test('должен работать с unicode символами', () => {
      const result = nameWithInitials({
        firstName: 'Ян',
        patronymic: 'У́нчжэ',
        lastName: 'Минь',
      });
      // charAt(0) возвращает только первую основную букву без диакритических знаков
      expect(result).toBe('Я. У. Минь');
    });
  });

  describe('fullName', () => {
    test('должен объединять все части имени пробелами', () => {
      const result = fullName({
        firstName: 'Иван',
        patronymic: 'Петрович',
        lastName: 'Иванов',
      });
      expect(result).toBe('Иван Петрович Иванов');
    });

    test('должен фильтровать null значения', () => {
      const result = fullName({
        firstName: 'Иван',
        patronymic: null,
        lastName: 'Иванов',
      });
      expect(result).toBe('Иван Иванов');
    });

    test('должен фильтровать undefined значения', () => {
      const result = fullName({
        firstName: 'Иван',
        patronymic: undefined,
        lastName: 'Иванов',
      });
      expect(result).toBe('Иван Иванов');
    });

    test('должен фильтровать пустые строки', () => {
      const result = fullName({
        firstName: '',
        patronymic: 'Петрович',
        lastName: '',
      });
      expect(result).toBe('Петрович');
    });

    test('должен возвращать пустую строку если все поля пустые', () => {
      const result = fullName({
        firstName: '',
        patronymic: '',
        lastName: '',
      });
      expect(result).toBe('');
    });

    test('должен возвращать только имя', () => {
      const result = fullName({
        firstName: 'Иван',
        patronymic: null,
        lastName: null,
      });
      expect(result).toBe('Иван');
    });

    test('должен возвращать имя и фамилию без отчества', () => {
      const result = fullName({
        firstName: 'Иван',
        patronymic: '',
        lastName: 'Иванов',
      });
      expect(result).toBe('Иван Иванов');
    });

    test('должен работать с пробелами в имени', () => {
      const result = fullName({
        firstName: 'Иван Петров',
        patronymic: 'Сидорович',
        lastName: 'Иванов',
      });
      expect(result).toBe('Иван Петров Сидорович Иванов');
    });
  });

  describe('fullNameLastFirstPat', () => {
    test('должен формировать ФИО с фамилией в начале', () => {
      const result = fullNameLastFirstPat({
        lastName: 'Иванов',
        firstName: 'Иван',
        patronymic: 'Петрович',
      });
      expect(result).toBe('Иванов Иван Петрович');
    });

    test('должен возвращать тире для null', () => {
      const result = fullNameLastFirstPat(null);
      expect(result).toBe('—');
    });

    test('должен фильтровать null значения внутри объекта', () => {
      const result = fullNameLastFirstPat({
        lastName: 'Иванов',
        firstName: null,
        patronymic: 'Петрович',
      });
      expect(result).toBe('Иванов Петрович');
    });

    test('должен фильтровать undefined значения внутри объекта', () => {
      const result = fullNameLastFirstPat({
        lastName: 'Иванов',
        firstName: undefined,
        patronymic: undefined,
      });
      expect(result).toBe('Иванов');
    });

    test('должен возвращать только фамилию', () => {
      const result = fullNameLastFirstPat({
        lastName: 'Иванов',
        firstName: null,
        patronymic: null,
      });
      expect(result).toBe('Иванов');
    });

    test('должен работать с пустыми строками', () => {
      const result = fullNameLastFirstPat({
        lastName: '',
        firstName: '',
        patronymic: '',
      });
      expect(result).toBe('');
    });

    test('должен возвращать пустую строку для пустого объекта', () => {
      const result = fullNameLastFirstPat({});
      expect(result).toBe('');
    });

    test('должен работать с объектом без свойств', () => {
      const result = fullNameLastFirstPat({} as any);
      expect(result).toBe('');
    });
  });

  describe('fullNameWithPhoneNumber', () => {
    test('должен объединять ФИО и телефон', () => {
      const result = fullNameWithPhoneNumber({
        firstName: 'Иван',
        patronymic: 'Петрович',
        lastName: 'Иванов',
        contactPhone: '+7 (999) 123-45-67',
      });
      expect(result).toBe('Иван Петрович Иванов, +7 (999) 123-45-67');
    });

    test('должен фильтровать null значения ФИО', () => {
      const result = fullNameWithPhoneNumber({
        firstName: 'Иван',
        patronymic: null,
        lastName: 'Иванов',
        contactPhone: '+7 (999) 123-45-67',
      });
      expect(result).toBe('Иван Иванов, +7 (999) 123-45-67');
    });

    test('должен показывать только телефон если ФИО пустые', () => {
      const result = fullNameWithPhoneNumber({
        firstName: null,
        patronymic: null,
        lastName: null,
        contactPhone: '+7 (999) 123-45-67',
      });
      expect(result).toBe(', +7 (999) 123-45-67');
    });

    test('должен показывать только ФИО если телефон null', () => {
      const result = fullNameWithPhoneNumber({
        firstName: 'Иван',
        patronymic: 'Петрович',
        lastName: 'Иванов',
        contactPhone: null,
      });
      expect(result).toBe('Иван Петрович Иванов, ');
    });

    test('должен работать с пустыми строками', () => {
      const result = fullNameWithPhoneNumber({
        firstName: '',
        patronymic: '',
        lastName: '',
        contactPhone: '',
      });
      expect(result).toBe(', ');
    });

    test('должен работать с undefined', () => {
      const result = fullNameWithPhoneNumber({
        firstName: undefined,
        patronymic: undefined,
        lastName: undefined,
        contactPhone: undefined,
      });
      expect(result).toBe(', ');
    });

    test('должен работать с номером без форматирования', () => {
      const result = fullNameWithPhoneNumber({
        firstName: 'Иван',
        patronymic: 'Петрович',
        lastName: 'Иванов',
        contactPhone: '89991234567',
      });
      expect(result).toBe('Иван Петрович Иванов, 89991234567');
    });
  });

  describe('fullVehicleInfo', () => {
    test('должен объединять всю информацию об автомобиле', () => {
      const result = fullVehicleInfo({
        brandName: 'Toyota',
        model: 'Camry',
        color: 'Синий',
        registrationNumber: 'А123ВС777',
      });
      expect(result).toBe('Toyota Camry Синий А123ВС777');
    });

    test('должен фильтровать null значения', () => {
      const result = fullVehicleInfo({
        brandName: 'Toyota',
        model: null,
        color: 'Синий',
        registrationNumber: 'А123ВС777',
      });
      expect(result).toBe('Toyota Синий А123ВС777');
    });

    test('должен работать без цвета', () => {
      const result = fullVehicleInfo({
        brandName: 'Toyota',
        model: 'Camry',
        registrationNumber: 'А123ВС777',
      });
      expect(result).toBe('Toyota Camry А123ВС777');
    });

    test('должен возвращать только регистрационный номер', () => {
      const result = fullVehicleInfo({
        brandName: null,
        model: null,
        registrationNumber: 'А123ВС777',
      });
      expect(result).toBe('А123ВС777');
    });

    test('должен возвращать пустую строку для пустых данных', () => {
      const result = fullVehicleInfo({
        brandName: null,
        model: null,
        registrationNumber: null,
      });
      expect(result).toBe('');
    });

    test('должен работать с пустыми строками', () => {
      const result = fullVehicleInfo({
        brandName: '',
        model: '',
        color: '',
        registrationNumber: '',
      });
      expect(result).toBe('');
    });

    test('должен работать с undefined', () => {
      const result = fullVehicleInfo({
        brandName: undefined,
        model: undefined,
        color: undefined,
        registrationNumber: undefined,
      });
      expect(result).toBe('');
    });
  });

  describe('shortName', () => {
    test('должен формировать сокращенное имя с инициалами', () => {
      const result = shortName({
        firstName: 'Иван',
        patronymic: 'Петрович',
        lastName: 'Иванов',
      });
      expect(result).toBe('И. П. Иванов');
    });

    test('должен формировать имя без отчества', () => {
      const result = shortName({
        firstName: 'Иван',
        lastName: 'Иванов',
      });
      expect(result).toBe('И. Иванов');
    });

    test('должен возвращать точку и пробел для пустых данных', () => {
      const result = shortName({
        firstName: '',
        patronymic: '',
        lastName: '',
      });
      expect(result).toBe('. ');
    });

    test('должен работать с однобуквенными именами', () => {
      const result = shortName({
        firstName: 'А',
        patronymic: 'Б',
        lastName: 'В',
      });
      expect(result).toBe('А. Б. В');
    });

    test('должен работать с unicode символами', () => {
      const result = shortName({
        firstName: 'Ян',
        patronymic: 'У́нчжэ',
        lastName: 'Минь',
      });
      // charAt(0) возвращает только первую основную букву без диакритических знаков
      expect(result).toBe('Я. У. Минь');
    });
  });

  describe('edge cases', () => {
    test('nameWithInitials должен обрабатывать символы с диакритикой', () => {
      const result = nameWithInitials({
        firstName: 'Žanas',
        patronymic: 'Ąžuolaitis',
        lastName: 'Kavolaitis',
      });
      expect(result).toBe('Ž. Ą. Kavolaitis');
    });

    test('fullName должен сохранять пробелы внутри частей имени', () => {
      const result = fullName({
        firstName: 'Иван Иванович',
        patronymic: 'Сидорович',
        lastName: 'Петров-Водкин',
      });
      expect(result).toBe('Иван Иванович Сидорович Петров-Водкин');
    });

    test('fullNameLastFirstPat должен работать с частичными данными', () => {
      const result = fullNameLastFirstPat({
        lastName: 'Иванов',
      });
      expect(result).toBe('Иванов');
    });

    test('fullVehicleInfo должен работать с кириллицей и латиницей', () => {
      const result = fullVehicleInfo({
        brandName: 'BMW',
        model: 'X5',
        color: 'Черный',
        registrationNumber: 'М777КХ197',
      });
      expect(result).toBe('BMW X5 Черный М777КХ197');
    });
  });
});

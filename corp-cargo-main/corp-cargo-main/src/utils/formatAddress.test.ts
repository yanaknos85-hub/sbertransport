import { formatAddress, formatAddressForRequest, transformAddress } from './formatAddress';

describe('formatAddress', () => {
  describe('null/undefined/пустые значения', () => {
    test('для null должен вернуть пустую строку', () => {
      expect(formatAddress(null)).toBe('');
    });

    test('для undefined должен вернуть пустую строку', () => {
      expect(formatAddress(undefined)).toBe('');
    });
  });

  describe('Waypoint', () => {
    test('должен собрать полный адрес со всеми полями', () => {
      const waypoint = {
        latitude: 55.75,
        longitude: 37.61,
        street: 'Lenina',
        house: '123',
        city: 'Moscow',
      };
      expect(formatAddress(waypoint)).toBe('Lenina, 123, Moscow');
    });

    test('должен работать только с улицей', () => {
      const waypoint = {
        latitude: 55.75,
        longitude: 37.61,
        street: 'Lenina',
      };
      expect(formatAddress(waypoint)).toBe('Lenina');
    });

    test('должен работать только с домом', () => {
      const waypoint = {
        latitude: 55.75,
        longitude: 37.61,
        house: '123',
      };
      expect(formatAddress(waypoint)).toBe('123');
    });

    test('должен работать только с городом', () => {
      const waypoint = {
        latitude: 55.75,
        longitude: 37.61,
        city: 'Moscow',
      };
      expect(formatAddress(waypoint)).toBe('Moscow');
    });

    test('должен собрать адрес с улицей и домом', () => {
      const waypoint = {
        latitude: 55.75,
        longitude: 37.61,
        street: 'Lenina',
        house: '123',
      };
      expect(formatAddress(waypoint)).toBe('Lenina, 123');
    });

    test('должен собрать адрес с улицей и городом', () => {
      const waypoint = {
        latitude: 55.75,
        longitude: 37.61,
        street: 'Lenina',
        city: 'Moscow',
      };
      expect(formatAddress(waypoint)).toBe('Lenina, Moscow');
    });

    test('должен собрать адрес с домом и городом', () => {
      const waypoint = {
        latitude: 55.75,
        longitude: 37.61,
        house: '123',
        city: 'Moscow',
      };
      expect(formatAddress(waypoint)).toBe('123, Moscow');
    });

    test('должен возвращать пустую строку для минимального waypoint', () => {
      const waypoint = {
        latitude: 55.75,
        longitude: 37.61,
      };
      expect(formatAddress(waypoint)).toBe('');
    });
  });

  describe('LocationAddress', () => {
    test('должен собрать полный адрес', () => {
      const locationAddress = {
        latitude: 55.75,
        longitude: 37.61,
        street: 'Tverskaya',
        house: '15',
        city: 'Moscow',
      };
      expect(formatAddress(locationAddress)).toBe('Tverskaya, 15, Moscow');
    });

    test('должен работать без дома', () => {
      const locationAddress = {
        latitude: 55.75,
        longitude: 37.61,
        street: 'Tverskaya',
        city: 'Moscow',
      };
      expect(formatAddress(locationAddress)).toBe('Tverskaya, Moscow');
    });
  });

  describe('с пустыми строками', () => {
    test('должен игнорировать пустые строки', () => {
      const waypoint = {
        latitude: 55.75,
        longitude: 37.61,
        street: '',
        house: '',
        city: '',
      };
      expect(formatAddress(waypoint)).toBe('');
    });

    test('должен игнорировать null и undefined значения в полях', () => {
      const waypoint = {
        latitude: 55.75,
        longitude: 37.61,
        street: undefined as unknown as string,
        house: null as unknown as string,
        city: '',
      };
      expect(formatAddress(waypoint)).toBe('');
    });
  });

  describe('с пробелами в значениях', () => {
    test('должен сохранять пробелы', () => {
      const waypoint = {
        latitude: 55.75,
        longitude: 37.61,
        street: 'Lenina Street',
        house: '123 A',
        city: 'Moscow City',
      };
      expect(formatAddress(waypoint)).toBe('Lenina Street, 123 A, Moscow City');
    });
  });

  describe('с дополнительными полями', () => {
    test('должен игнорировать дополнительные поля', () => {
      const waypoint = {
        latitude: 55.75,
        longitude: 37.61,
        street: 'Lenina',
        house: '123',
        city: 'Moscow',
        country: 'Russia',
        region: 'Moscow region',
        building: '5',
        structure: '2',
        district: 'Central',
      };
      expect(formatAddress(waypoint)).toBe('Lenina, 123, Moscow');
    });
  });
});

describe('formatAddressForRequest', () => {
  describe('null/undefined/пустые значения', () => {
    test('для null должен вернуть пустую строку', () => {
      expect(formatAddressForRequest(null as unknown as string)).toBe('');
    });

    test('для undefined должен вернуть пустую строку', () => {
      expect(formatAddressForRequest(undefined as unknown as string)).toBe('');
    });
  });

  test('должен удалить все запятые из строки', () => {
    expect(formatAddressForRequest('Lenina, 123, Moscow')).toBe('Lenina 123 Moscow');
  });

  test('должен работать без запятых', () => {
    expect(formatAddressForRequest('Lenina 123 Moscow')).toBe('Lenina 123 Moscow');
  });

  test('должен работать с одной запятой', () => {
    expect(formatAddressForRequest('Lenina, 123')).toBe('Lenina 123');
  });

  test('должен работать с несколькими подряд запятыми', () => {
    expect(formatAddressForRequest('Lenina,, 123,,, Moscow')).toBe('Lenina 123 Moscow');
  });

  test('должен сохранять пробелы', () => {
    expect(formatAddressForRequest('Lenina Street, 123 A, Moscow City')).toBe('Lenina Street 123 A Moscow City');
  });
});

describe('transformAddress', () => {
  test('должен удалить первые два элемента, если их >= 4', () => {
    const address = 'Country, Region, City, Street, House';
    expect(transformAddress(address)).toBe('City, Street, House');
  });

  test('должен оставить адрес без изменений, если меньше 4 элементов', () => {
    const address = 'City, Street, House';
    expect(transformAddress(address)).toBe('City, Street, House');
  });

  test('должен работать с тремя элементами', () => {
    const address = 'City, Street, House';
    expect(transformAddress(address)).toBe('City, Street, House');
  });

  test('должен работать с двумя элементами', () => {
    const address = 'Street, House';
    expect(transformAddress(address)).toBe('Street, House');
  });

  test('должен работать с одним элементом', () => {
    const address = 'Street';
    expect(transformAddress(address)).toBe('Street');
  });

  test('должен работать с пустой строкой', () => {
    const address = '';
    expect(transformAddress(address)).toBe('');
  });

  test('должен работать с четырьмя элементами', () => {
    const address = 'Country, Region, City, Street';
    expect(transformAddress(address)).toBe('City, Street');
  });

  test('должен сохранять пробелы после запятых', () => {
    const address = 'Russia, Moscow region, Moscow, Lenina Street, 123';
    expect(transformAddress(address)).toBe('Moscow, Lenina Street, 123');
  });
});

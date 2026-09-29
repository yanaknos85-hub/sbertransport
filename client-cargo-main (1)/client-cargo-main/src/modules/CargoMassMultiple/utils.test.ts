// Mock moment to avoid date dependencies
const mockDate = new Date('2024-01-15T10:30:00.000Z');
const mockMomentValue = mockDate.getTime();

jest.mock('moment', () => {
  const mockMoment = (date?: any) => {
    const m = {
      utc: () => ({
        format: (format: string) => {
          if (format === 'DD.MM.YYYY HH:mm') {
            return '15.01.2024 10:30';
          }
          if (format === 'DD.MM.YYYY') {
            return '15.01.2024';
          }
          return '2024-01-15T10:30:00Z';
        },
        set: (time: any) => ({
          toDate: () => {
            const d = new Date(mockDate);
            if (time.hours !== undefined) d.setUTCHours(time.hours);
            if (time.minutes !== undefined) d.setUTCMinutes(time.minutes);
            if (time.seconds !== undefined) d.setUTCSeconds(time.seconds);
            return d;
          },
        }),
        utcOffset: () => 180,
      }),
      utcOffset: () => 180,
      valueOf: () => mockMomentValue,
    };
    return m;
  };
  mockMoment.utc = (date?: any) => mockMoment(date);
  return mockMoment;
});

describe('CargoMassMultiple utils', () => {
  describe('buildAddressString', () => {
    it('should build full address string with all fields', () => {
      const { buildAddressString } = require('./utils');

      const waypoint = {
        region: 'Московская область',
        district: 'Центральный район',
        city: 'Москва',
        livingArea: 'Левобережный',
        settlement: 'Посон',
        place: 'Посёлок',
        street: 'Улица Ленина',
        house: '15',
        building: '2',
        structure: 'стр. 1',
      };

      const result = buildAddressString(waypoint);

      expect(result).toBe(
        'Московская область,  Центральный район,  Москва,  Левобережный,  Посон,  Посёлок,  Улица Ленина, 15, 2, стр. 1',
      );
    });

    it('should handle address with minimal fields', () => {
      const { buildAddressString } = require('./utils');

      const waypoint = {
        city: 'Санкт-Петербург',
        street: 'Невский проспект',
        house: '28',
      };

      const result = buildAddressString(waypoint);

      expect(result).toBe('Санкт-Петербург,     Невский проспект, 28');
    });

    it('should handle address with undefined fields', () => {
      const { buildAddressString } = require('./utils');

      const waypoint = {
        region: 'Московская область',
        city: undefined,
        street: 'Тверская',
      };

      const result = buildAddressString(waypoint);

      expect(result).toBe('Московская область,       Тверская');
    });

    it('should handle empty waypoint object', () => {
      const { buildAddressString } = require('./utils');

      const waypoint = {};

      const result = buildAddressString(waypoint);

      expect(result).toBe('');
    });

    it('should handle null waypoint', () => {
      const { buildAddressString } = require('./utils');

      // eslint-disable-next-line no-null/no-null
      const result = buildAddressString(null as any);

      expect(result).toBe('');
    });

    it('should replace first space in city name with single space', () => {
      const { buildAddressString } = require('./utils');

      const waypoint = {
        city: ' Новосибирск ',
        street: 'Карла Маркса',
      };

      const result = buildAddressString(waypoint);

      expect(result).toBe('Новосибирск ,     Карла Маркса');
    });

    it('should handle undefined district and livingArea', () => {
      const { buildAddressString } = require('./utils');

      const waypoint = {
        region: 'Свердловская область',
        district: undefined,
        city: 'Екатеринбург',
        livingArea: undefined,
        street: 'ulitsa Lenina',
      };

      const result = buildAddressString(waypoint);

      expect(result).toBe('Свердловская область,   Екатеринбург,     ulitsa Lenina');
    });

    it('should handle only street and house', () => {
      const { buildAddressString } = require('./utils');

      const waypoint = {
        street: 'Пушкина',
        house: '10',
      };

      const result = buildAddressString(waypoint);

      expect(result).toBe('Пушкина, 10');
    });

    it('should handle all fields except structure', () => {
      const { buildAddressString } = require('./utils');

      const waypoint = {
        region: 'Республика Татарстан',
        district: 'Кировский',
        city: 'Казань',
        livingArea: 'Северный',
        settlement: 'Микрорайон',
        place: 'Жилой массив',
        street: 'Московская',
        house: '55',
        building: 'корп. 1',
        structure: undefined,
      };

      const result = buildAddressString(waypoint);

      expect(result).toBe(
        'Республика Татарстан,  Кировский,  Казань,  Северный,  Микрорайон,  Жилой массив,  Московская, 55, корп. 1',
      );
    });

    it('should handle spaces in street name', () => {
      const { buildAddressString } = require('./utils');

      const waypoint = {
        city: 'Ростов-на-Дону',
        street: 'Пушкинская     ',
        house: '12',
      };

      const result = buildAddressString(waypoint);

      expect(result).toBe('Ростов-на-Дону,     Пушкинская     , 12');
    });
  });

  describe('buildDate', () => {
    it('should format timestamp to date with time', () => {
      const { buildDate } = require('./utils');

      const timestamp = 1705315800000;

      const result = buildDate(timestamp);

      expect(result).toBe('15.01.2024 10:30');
    });

    it('should handle zero timestamp', () => {
      const { buildDate } = require('./utils');

      const timestamp = 0;

      const result = buildDate(timestamp);

      expect(result).toBe('15.01.2024 10:30');
    });

    it('should handle current date', () => {
      const { buildDate } = require('./utils');

      const timestamp = Date.now();

      const result = buildDate(timestamp);

      expect(result).toBe('15.01.2024 10:30');
    });
  });

  describe('buildDateRegular', () => {
    it('should format timestamp to date in reverted format', () => {
      const { buildDateRegular } = require('./utils');

      const timestamp = 1705315800000;

      const result = buildDateRegular(timestamp);

      expect(result).toBe('15.01.2024');
    });

    it('should handle zero timestamp', () => {
      const { buildDateRegular } = require('./utils');

      const timestamp = 0;

      const result = buildDateRegular(timestamp);

      expect(result).toBe('15.01.2024');
    });

    it('should handle current date', () => {
      const { buildDateRegular } = require('./utils');

      const timestamp = Date.now();

      const result = buildDateRegular(timestamp);

      expect(result).toBe('15.01.2024');
    });
  });

  describe('desiredFormatDate', () => {
    it('should set time to 12:00:00 and return unix timestamp', () => {
      const { desiredFormatDate } = require('./utils');

      const desiredDate = new Date('2024-01-15T10:30:00.000Z');

      const result = desiredFormatDate(desiredDate);

      expect(result).toBe(1705320000000);
    });

    it('should handle positive offset (e.g. +03:00)', () => {
      const { getUnixDateWithOffset } = require('./utils');
      const moment = require('moment');

      const desiredDate = moment('2024-01-15T10:30:00.000Z').utcOffset(180);

      const result = getUnixDateWithOffset(desiredDate);

      expect(result).toBe(1705325400000);
    });
  });

  describe('getUnixDateWithOffset', () => {
    it('should calculate unix timestamp with UTC offset', () => {
      const { getUnixDateWithOffset } = require('./utils');
      const moment = require('moment');

      const desiredDate = moment('2024-01-15T10:30:00.000Z');

      const result = getUnixDateWithOffset(desiredDate);

      expect(result).toBe(1705325400000);
    });

    it('should handle zero offset', () => {
      const { getUnixDateWithOffset } = require('./utils');
      const moment = require('moment');

      const desiredDate = moment('2024-01-15T10:30:00.000Z').utc();

      const result = getUnixDateWithOffset(desiredDate);

      expect(result).toBe(1705325400000);
    });

    it('should handle positive offset (e.g. +03:00)', () => {
      const { getUnixDateWithOffset } = require('./utils');
      const moment = require('moment');

      const desiredDate = moment('2024-01-15T10:30:00.000Z').utcOffset(180);

      const result = getUnixDateWithOffset(desiredDate);

      expect(result).toBe(1705325400000);
    });
  });
});

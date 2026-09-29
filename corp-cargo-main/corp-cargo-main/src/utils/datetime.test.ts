import moment from 'moment';
import { FORMAT, VALUE_NOT_FOUND, getYearRange, getFormatDate, localTimeStringToUtcString, utcTimeStringToLocalMoment } from './datetime';

describe('datetime', () => {
  beforeAll(() => {
    // Устанавливаем фиксированную дату для тестов
    const fixedDate = new Date('2026-05-08T10:30:00Z');
    jest.spyOn(global.Date, 'now').mockImplementation(() => fixedDate.getTime());
  });

  afterAll(() => {
    jest.restoreAllMocks();
  });

  describe('FORMAT', () => {
    test('должен содержать правильный формат даты и времени', () => {
      expect(FORMAT).toBe('DD.MM.YYYY, HH:mm');
    });
  });

  describe('VALUE_NOT_FOUND', () => {
    test('должен содержать значение по умолчанию для пустых данных', () => {
      expect(VALUE_NOT_FOUND).toBe('-');
    });
  });

  describe('localTimeStringToUtcString', () => {
    test('должен конвертировать локальное время в UTC строку', () => {
      const localTime = '2026-05-08T12:00:00+03:00';
      const utcString = localTimeStringToUtcString(localTime);
      expect(utcString).toContain('T');
      expect(utcString).toContain('Z');
    });

    test('должен правильно конвертировать время', () => {
      const localTime = '2026-05-08T15:00:00+03:00';
      const utcString = localTimeStringToUtcString(localTime);
      // 15:00 +03:00 = 12:00 UTC
      expect(utcString).toContain('12:00:00');
    });
  });

  describe('utcTimeStringToLocalMoment', () => {
    test('должен конвертировать UTC строку в локальный moment', () => {
      const utcTime = '2026-05-08T12:00:00Z';
      const localMoment = utcTimeStringToLocalMoment(utcTime);
      expect(localMoment).toBeDefined();
      expect(localMoment.isValid()).toBe(true);
    });

    test('должен правильно конвертировать время в локальный часовой пояс', () => {
      const utcTime = '2026-05-08T12:00:00Z';
      const localMoment = utcTimeStringToLocalMoment(utcTime);
      // UTC 12:00 = 15:00 (MSK, +3)
      expect(localMoment.hour()).toBe(15);
    });
  });

  describe('getYearRange', () => {
    const currentYear = 2026;

    test('должен возвращать диапазон лет по умолчанию (5 лет до и после)', () => {
      const years = getYearRange();
      expect(years).toHaveLength(11); // 5 до + текущий + 5 после = 11
      expect(years[0]).toBe(currentYear - 5); // 2021
      expect(years[10]).toBe(currentYear + 5); // 2031
    });

    test('должен возвращать правильный диапазон с кастомными параметрами', () => {
      const years = getYearRange({ before: 3, after: 2 });
      expect(years).toHaveLength(6); // 3 до + текущий + 2 после = 6
      expect(years[0]).toBe(currentYear - 3); // 2023
      expect(years[5]).toBe(currentYear + 2); // 2028
    });

    test('должен возвращать пустой массив для нулевых параметров', () => {
      const years = getYearRange({ before: 0, after: 0 });
      expect(years).toHaveLength(1);
      expect(years[0]).toBe(currentYear);
    });

    test('должен возвращать только прошлые годы', () => {
      const years = getYearRange({ before: 3, after: 0 });
      expect(years).toHaveLength(4);
      expect(years[0]).toBe(currentYear - 3);
      expect(years[3]).toBe(currentYear);
    });

    test('должен возвращать только будущие годы', () => {
      const years = getYearRange({ before: 0, after: 3 });
      expect(years).toHaveLength(4);
      expect(years[0]).toBe(currentYear);
      expect(years[3]).toBe(currentYear + 3);
    });
  });

  describe('getFormatDate', () => {
    test('должен форматировать дату по умолчанию', () => {
      const date = '2026-05-08T10:30:00';
      const formatted = getFormatDate(date);
      expect(formatted).toBe('08.05.2026, 10:30');
    });

    test('должен возвращать VALUE_NOT_FOUND для undefined', () => {
      expect(getFormatDate(undefined)).toBe('-');
    });

    test('должен возвращать VALUE_NOT_FOUND для null', () => {
      expect(getFormatDate(null as unknown as undefined)).toBe('-');
    });

    test('должен возвращать VALUE_NOT_FOUND для пустой строки', () => {
      expect(getFormatDate('')).toBe('-');
    });

    test('должен возвращать VALUE_NOT_FOUND для 0', () => {
      expect(getFormatDate(0)).toBe('-');
    });

    test('должен использовать кастомный формат', () => {
      const date = '2026-05-08';
      const formatted = getFormatDate(date, 'YYYY-MM-DD');
      expect(formatted).toBe('2026-05-08');
    });

    test('должен использовать кастомное значение для пустых данных', () => {
      expect(getFormatDate(undefined, FORMAT, 'N/A')).toBe('N/A');
    });

    test('должен форматировать timestamp', () => {
      const timestamp = 1778225400000; // 2026-05-08T10:30:00+03:00
      const formatted = getFormatDate(timestamp);
      expect(formatted).toBe('08.05.2026, 10:30');
    });

    test('должен использовать русскую локаль для форматирования', () => {
      const date = '2026-01-01';
      const formatted = getFormatDate(date, 'D MMMM YYYY');
      expect(formatted).toBe('1 января 2026');
    });
  });
});

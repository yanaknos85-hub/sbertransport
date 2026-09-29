import moment from 'moment';
import {
  formatTime,
  formatShortTime,
  formatBaseTime,
  formatBaseDate,
  formatTimeDate,
  getTimeString,
  formatBaseUtcDate,
} from './formatTime';
import {
  DATE_FORMAT,
} from 'constants/constants.app';

describe('formatTime', () => {
  test('для массива [2024, 1, 15, 10, 30, 45] должен вернуть дату с добавлением 4 часов', () => {
    const result = formatTime([2024, 1, 15, 10, 30, 45]);
    expect(result).toBe('15.01.2024. 14:30:45');
  });

  test('для строки ISO даты должен вернуть отформатированную дату (без добавления часов)', () => {
    const result = formatTime('2024-01-15T10:30:45');
    expect(result).toBe('15.01.2024. 10:30:45');
  });

  test('для timestamp (числа) должен вернуть отформатированную дату (без добавления часов)', () => {
    const timestamp = new Date(2024, 0, 15, 10, 30, 45).getTime();
    const result = formatTime(timestamp);
    expect(result).toBe('15.01.2024. 10:30:45');
  });

  test('для undefined должен вернуть значение по умолчанию "-"', () => {
    expect(formatTime(undefined)).toBe('-');
  });

  test('для null должен вернуть значение по умолчанию "-"', () => {
    expect(formatTime(null)).toBe('-');
  });

  test('для кастомного emptyValue должен использовать его', () => {
    expect(formatTime(undefined, '—')).toBe('—');
    expect(formatTime(null, '—')).toBe('—');
  });
});

describe('formatShortTime', () => {
  test('для валидной даты должен вернуть время в формате "мин"', () => {
    const result = formatShortTime(0);
    expect(result).toBe(`${moment(0).format(DATE_FORMAT.TIME_SHORT_MINUTE)} мин`);
  });

  test('для 0 должен вернуть время в формате "мин"', () => {
    const result = formatShortTime(0);
    expect(result).toBe(`${moment(0).format(DATE_FORMAT.TIME_SHORT_MINUTE)} мин`);
  });

  test('для undefined должен вернуть значение по умолчанию "-"', () => {
    expect(formatShortTime(undefined)).toBe('-');
  });

  test('для null должен вернуть значение по умолчанию "-"', () => {
    expect(formatShortTime(null)).toBe('-');
  });

  test('для кастомного emptyValue должен использовать его', () => {
    expect(formatShortTime(undefined, '—')).toBe('—');
    expect(formatShortTime(null, '—')).toBe('—');
  });
});

describe('formatBaseTime', () => {
  test('для валидной даты должен вернуть время в формате HH:mm:ss', () => {
    const result = formatBaseTime(0);
    expect(result).toBe(moment(0).format(DATE_FORMAT.TIME_BASE));
  });

  test('для 0 должен вернуть время в формате HH:mm:ss', () => {
    const result = formatBaseTime(0);
    expect(result).toBe(moment(0).format(DATE_FORMAT.TIME_BASE));
  });

  test('для undefined должен вернуть значение по умолчанию "-"', () => {
    expect(formatBaseTime(undefined)).toBe('-');
  });

  test('для null должен вернуть значение по умолчанию "-"', () => {
    expect(formatBaseTime(null)).toBe('-');
  });

  test('для кастомного emptyValue должен использовать его', () => {
    expect(formatBaseTime(undefined, '—')).toBe('—');
    expect(formatBaseTime(null, '—')).toBe('—');
  });
});

describe('formatBaseDate', () => {
  test('для валидной даты должен вернуть дату в формате DD.MM.YYYY', () => {
    const result = formatBaseDate(1000);
    expect(result).toBe(moment(1000).format(DATE_FORMAT.BASE_REVERTED_DOTS));
  });

  test('для 0 должен вернуть "-" (так как 0 falsy)', () => {
    expect(formatBaseDate(0)).toBe('-');
  });

  test('для undefined должен вернуть значение по умолчанию "-"', () => {
    expect(formatBaseDate(undefined)).toBe('-');
  });

  test('для null должен вернуть значение по умолчанию "-"', () => {
    expect(formatBaseDate(null)).toBe('-');
  });

  test('для кастомного emptyValue должен использовать его', () => {
    expect(formatBaseDate(undefined, '—')).toBe('—');
    expect(formatBaseDate(null, '—')).toBe('—');
  });
});

describe('formatTimeDate', () => {
  test('для валидной даты должен вернуть дату со временем в формате DD.MM.YYYY HH:mm', () => {
    const result = formatTimeDate(1000);
    expect(result).toBe(moment(1000).format(DATE_FORMAT.DATE_WITH_TIME_DOTS));
  });

  test('для 0 должен вернуть "-" (так как 0 falsy)', () => {
    expect(formatTimeDate(0)).toBe('-');
  });

  test('для undefined должен вернуть значение по умолчанию "-"', () => {
    expect(formatTimeDate(undefined)).toBe('-');
  });

  test('для null должен вернуть значение по умолчанию "-"', () => {
    expect(formatTimeDate(null)).toBe('-');
  });

  test('для кастомного emptyValue должен использовать его', () => {
    expect(formatTimeDate(undefined, '—')).toBe('—');
    expect(formatTimeDate(null, '—')).toBe('—');
  });
});

describe('getTimeString', () => {
  test('для 60000 ms (1 минута) должен вернуть "1 мин"', () => {
    expect(getTimeString(60000)).toBe('1 мин');
  });

  test('для 120000 ms (2 минуты) должен вернуть "2 мин"', () => {
    expect(getTimeString(120000)).toBe('2 мин');
  });

  test('для 3600000 ms (1 час) должен вернуть "1 ч 0 мин"', () => {
    expect(getTimeString(3600000)).toBe('1 ч 0 мин');
  });

  test('для 5400000 ms (1.5 часа) должен вернуть "1 ч 30 мин"', () => {
    expect(getTimeString(5400000)).toBe('1 ч 30 мин');
  });

  test('для 7265000 ms (2 часа 1 минута) должен вернуть "2 ч 1 мин"', () => {
    expect(getTimeString(7265000)).toBe('2 ч 1 мин');
  });

  test('для строки "60000" должен вернуть "1 мин"', () => {
    expect(getTimeString('60000')).toBe('1 мин');
  });

  test('для undefined должен вернуть значение по умолчанию "-"', () => {
    expect(getTimeString(undefined)).toBe('-');
  });

  test('для null должен вернуть значение по умолчанию "-"', () => {
    expect(getTimeString(null)).toBe('-');
  });

  test('для кастомного emptyValue должен использовать его', () => {
    expect(getTimeString(undefined, '—')).toBe('—');
    expect(getTimeString(null, '—')).toBe('—');
  });

  test('для 0 должен вернуть "0 мин"', () => {
    expect(getTimeString(0)).toBe('0 мин');
  });

  test('для отрицательного числа должен вернуть отрицательное время', () => {
    expect(getTimeString(-60000)).toBe('-1 мин');
  });
});

describe('formatBaseUtcDate', () => {
  test('для валидной даты должен вернуть дату в UTC формате', () => {
    const result = formatBaseUtcDate(1000);
    expect(result).toBe(moment.utc(1000).format(DATE_FORMAT.DATE_WITH_UTC_TIME));
  });

  test('для 0 должен вернуть "-" (так как 0 falsy)', () => {
    expect(formatBaseUtcDate(0)).toBe('-');
  });

  test('для undefined должен вернуть значение по умолчанию "-"', () => {
    expect(formatBaseUtcDate(undefined)).toBe('-');
  });

  test('для null должен вернуть значение по умолчанию "-"', () => {
    expect(formatBaseUtcDate(null)).toBe('-');
  });

  test('для кастомного emptyValue должен использовать его', () => {
    expect(formatBaseUtcDate(undefined, '—')).toBe('—');
    expect(formatBaseUtcDate(null, '—')).toBe('—');
  });
});

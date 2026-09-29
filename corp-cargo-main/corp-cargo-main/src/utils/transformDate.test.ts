import moment from 'moment';
import { transformDate } from './transformDate';

describe('transformDate', () => {
  test('для undefined должен вернуть undefined', () => {
    expect(transformDate(undefined)).toBeUndefined();
  });

  test('для кортежа валидных моментов должен вернуть объект', () => {
    const start = moment('2026-05-08T10:00:00');
    const end = moment('2026-05-09T15:00:00');

    const result = transformDate([start, end]);

    expect(result).toBeDefined();
    expect(result).not.toBeNull();
    expect(typeof result).toBe('object');
  });

  test('start должен соответствовать началу дня первого момента', () => {
    const start = moment('2026-05-08T15:30:45.123');
    const end = moment('2026-05-09T10:00:00');

    const result = transformDate([start, end]);

    expect(result?.start).toBe(start.clone().startOf('day').valueOf());
    expect(result?.start).toBe(moment('2026-05-08').startOf('day').valueOf());
  });

  test('start должен сбрасывать часы, минуты, секунды и миллисекунды в ноль', () => {
    const start = moment('2026-05-08T23:59:59.999');
    const end = moment('2026-05-09T00:00:00');

    const result = transformDate([start, end]);

    const resultStart = moment(result?.start);
    expect(resultStart.hour()).toBe(0);
    expect(resultStart.minute()).toBe(0);
    expect(resultStart.second()).toBe(0);
    expect(resultStart.millisecond()).toBe(0);
  });

  test('end должен соответствовать концу дня второго момента', () => {
    const start = moment('2026-05-08T10:00:00');
    const end = moment('2026-05-09T05:15:20.500');

    const result = transformDate([start, end]);

    expect(result?.end).toBe(end.clone().endOf('day').valueOf());
    expect(result?.end).toBe(moment('2026-05-09').endOf('day').valueOf());
  });

  test('end должен устанавливать часы 23, минуты 59, секунды 59 и миллисекунды 999', () => {
    const start = moment('2026-05-08T10:00:00');
    const end = moment('2026-05-09T00:00:00');

    const result = transformDate([start, end]);

    const resultEnd = moment(result?.end);
    expect(resultEnd.hour()).toBe(23);
    expect(resultEnd.minute()).toBe(59);
    expect(resultEnd.second()).toBe(59);
    expect(resultEnd.millisecond()).toBe(999);
  });

  test('для одинаковых дат в кортеже должен вернуть начало и конец одного дня', () => {
    const sameDay = moment('2026-05-08T12:00:00');

    const result = transformDate([sameDay, sameDay]);

    expect(result?.start).toBe(moment('2026-05-08').startOf('day').valueOf());
    expect(result?.end).toBe(moment('2026-05-08').endOf('day').valueOf());
  });

  test('end должен быть строго позже start для диапазона из разных дней', () => {
    const start = moment('2026-05-08');
    const end = moment('2026-05-15');

    const result = transformDate([start, end]);

    expect(result?.end).toBeGreaterThan(result?.start as number);
  });

  test('возвращаемый объект должен содержать только поля start и end', () => {
    const start = moment('2026-05-08');
    const end = moment('2026-05-09');

    const result = transformDate([start, end]);

    expect(Object.keys(result!)).toEqual(['start', 'end']);
  });

  test('start и end должны быть числами (timestamp в миллисекундах)', () => {
    const start = moment('2026-05-08');
    const end = moment('2026-05-09');

    const result = transformDate([start, end]);

    expect(typeof result?.start).toBe('number');
    expect(typeof result?.end).toBe('number');
  });

  test('для дат на границе месяцев должен корректно выставлять начало и конец дня', () => {
    const start = moment('2026-04-30T20:00:00');
    const end = moment('2026-05-01T05:00:00');

    const result = transformDate([start, end]);

    expect(result?.start).toBe(moment('2026-04-30').startOf('day').valueOf());
    expect(result?.end).toBe(moment('2026-05-01').endOf('day').valueOf());
  });
});

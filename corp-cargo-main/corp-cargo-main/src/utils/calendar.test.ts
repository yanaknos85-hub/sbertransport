import { monthNames, monthNamesEng, monthNamesShort, monthNamesEngShort, convertMonthNumToString, normalizeMonth } from './calendar';

describe('calendar', () => {
  describe('monthNames', () => {
    test('должен содержать 12 месяцев на русском языке', () => {
      expect(monthNames).toHaveLength(12);
      expect(monthNames[0]).toBe('Январь');
      expect(monthNames[11]).toBe('Декабрь');
    });

    test('должен содержать правильные названия месяцев', () => {
      expect(monthNames).toEqual([
        'Январь',
        'Февраль',
        'Март',
        'Апрель',
        'Май',
        'Июнь',
        'Июль',
        'Август',
        'Сентябрь',
        'Октябрь',
        'Ноябрь',
        'Декабрь',
      ]);
    });
  });

  describe('monthNamesEng', () => {
    test('должен содержать 12 месяцев на английском языке', () => {
      expect(monthNamesEng).toHaveLength(12);
      expect(monthNamesEng[0]).toBe('JANUARY');
      expect(monthNamesEng[11]).toBe('DECEMBER');
    });

    test('должен содержать правильные названия месяцев на английском', () => {
      expect(monthNamesEng).toEqual([
        'JANUARY',
        'FEBRUARY',
        'MARCH',
        'APRIL',
        'MAY',
        'JUNE',
        'JULY',
        'AUGUST',
        'SEPTEMBER',
        'OCTOBER',
        'NOVEMBER',
        'DECEMBER',
      ]);
    });
  });

  describe('monthNamesShort', () => {
    test('должен содержать 12 сокращенных названий месяцев на русском', () => {
      expect(monthNamesShort).toHaveLength(12);
      expect(monthNamesShort[0]).toBe('Янв');
      expect(monthNamesShort[11]).toBe('Дек');
    });

    test('должен содержать правильные сокращения', () => {
      expect(monthNamesShort).toEqual(['Янв', 'Фев', 'Март', 'Апр', 'Май', 'Июнь', 'Июль', 'Авг', 'Сент', 'Окт', 'Ноя', 'Дек']);
    });
  });

  describe('monthNamesEngShort', () => {
    test('должен содержать 12 сокращенных названий месяцев на английском', () => {
      expect(monthNamesEngShort).toHaveLength(12);
      expect(monthNamesEngShort[0]).toBe('Jan');
      expect(monthNamesEngShort[11]).toBe('Dec');
    });

    test('должен содержать правильные сокращения на английском', () => {
      expect(monthNamesEngShort).toEqual(['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']);
    });
  });

  describe('convertMonthNumToString', () => {
    test('должен возвращать полное название месяца по умолчанию', () => {
      expect(convertMonthNumToString(0)).toBe('Январь');
      expect(convertMonthNumToString(11)).toBe('Декабрь');
    });

    test('должен возвращать полное название месяца на английском', () => {
      expect(convertMonthNumToString(0, { eng: true })).toBe('JANUARY');
      expect(convertMonthNumToString(11, { eng: true })).toBe('DECEMBER');
    });

    test('должен возвращать сокращенное название месяца', () => {
      expect(convertMonthNumToString(0, { short: true })).toBe('Янв');
      expect(convertMonthNumToString(11, { short: true })).toBe('Дек');
    });

    test('должен возвращать сокращенное название месяца на английском', () => {
      expect(convertMonthNumToString(0, { short: true, eng: true })).toBe('Jan');
      expect(convertMonthNumToString(11, { short: true, eng: true })).toBe('Dec');
    });

    test('должен возвращать название месяца в нижнем регистре', () => {
      expect(convertMonthNumToString(0, { lowercase: true })).toBe('январь');
      expect(convertMonthNumToString(11, { lowercase: true })).toBe('декабрь');
    });

    test('должен возвращать английское название месяца в нижнем регистре', () => {
      expect(convertMonthNumToString(0, { lowercase: true, eng: true })).toBe('january');
      expect(convertMonthNumToString(11, { lowercase: true, eng: true })).toBe('december');
    });

    test('должен возвращать сокращенное английское название месяца в нижнем регистре', () => {
      expect(convertMonthNumToString(0, { lowercase: true, short: true, eng: true })).toBe('jan');
      expect(convertMonthNumToString(11, { lowercase: true, short: true, eng: true })).toBe('dec');
    });

    test('должен возвращать сокращенное название месяца в нижнем регистре', () => {
      expect(convertMonthNumToString(0, { lowercase: true, short: true })).toBe('янв');
      expect(convertMonthNumToString(11, { lowercase: true, short: true })).toBe('дек');
    });

    test('должен обрабатывать все месяцы корректно', () => {
      expect(convertMonthNumToString(1)).toBe('Февраль');
      expect(convertMonthNumToString(2)).toBe('Март');
      expect(convertMonthNumToString(3)).toBe('Апрель');
      expect(convertMonthNumToString(4)).toBe('Май');
      expect(convertMonthNumToString(5)).toBe('Июнь');
      expect(convertMonthNumToString(6)).toBe('Июль');
      expect(convertMonthNumToString(7)).toBe('Август');
      expect(convertMonthNumToString(8)).toBe('Сентябрь');
      expect(convertMonthNumToString(9)).toBe('Октябрь');
      expect(convertMonthNumToString(10)).toBe('Ноябрь');
    });

    test('должен обрабатывать все английские месяцы корректно', () => {
      expect(convertMonthNumToString(1, { eng: true })).toBe('FEBRUARY');
      expect(convertMonthNumToString(2, { eng: true })).toBe('MARCH');
      expect(convertMonthNumToString(3, { eng: true })).toBe('APRIL');
      expect(convertMonthNumToString(4, { eng: true })).toBe('MAY');
      expect(convertMonthNumToString(5, { eng: true })).toBe('JUNE');
      expect(convertMonthNumToString(6, { eng: true })).toBe('JULY');
      expect(convertMonthNumToString(7, { eng: true })).toBe('AUGUST');
      expect(convertMonthNumToString(8, { eng: true })).toBe('SEPTEMBER');
      expect(convertMonthNumToString(9, { eng: true })).toBe('OCTOBER');
      expect(convertMonthNumToString(10, { eng: true })).toBe('NOVEMBER');
    });
  });

  describe('normalizeMonth', () => {
    test('должен возвращать полное английское название месяца', () => {
      expect(normalizeMonth(0)).toBe('JANUARY');
      expect(normalizeMonth(11)).toBe('DECEMBER');
    });

    test('должен возвращать правильные названия месяцев', () => {
      expect(normalizeMonth(1)).toBe('FEBRUARY');
      expect(normalizeMonth(2)).toBe('MARCH');
      expect(normalizeMonth(3)).toBe('APRIL');
      expect(normalizeMonth(4)).toBe('MAY');
      expect(normalizeMonth(5)).toBe('JUNE');
      expect(normalizeMonth(6)).toBe('JULY');
      expect(normalizeMonth(7)).toBe('AUGUST');
      expect(normalizeMonth(8)).toBe('SEPTEMBER');
      expect(normalizeMonth(9)).toBe('OCTOBER');
      expect(normalizeMonth(10)).toBe('NOVEMBER');
    });
  });
});

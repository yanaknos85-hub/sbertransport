import { formatRubles, formatRublesWithoutPennies } from './formatRubles';

describe('formatRubles', () => {
  describe('форматирование с валютным значком (по умолчанию)', () => {
    test('должен форматировать положительное число с дробной частью', () => {
      expect(formatRubles(1234.56)).toBe('1 234,56 ₽');
    });

    test('должен форматировать целое число', () => {
      expect(formatRubles(1000)).toBe('1 000,00 ₽');
      expect(formatRubles(500)).toBe('500,00 ₽');
    });

    test('должен форматировать дробное число', () => {
      expect(formatRubles(123.45)).toBe('123,45 ₽');
      expect(formatRubles(0.99)).toBe('0,99 ₽');
    });

    test('должен форматировать отрицательное число', () => {
      expect(formatRubles(-500)).toBe('-500,00 ₽');
      expect(formatRubles(-1234.56)).toBe('-1 234,56 ₽');
    });

    test('должен форматировать ноль', () => {
      expect(formatRubles(0)).toBe('0,00 ₽');
    });

    test('должен форматировать очень большое число', () => {
      expect(formatRubles(1000000)).toBe('1 000 000,00 ₽');
    });

    test('должен форматировать число с множеством знаков после запятой', () => {
      expect(formatRubles(123.456)).toBe('123,46 ₽');
    });
  });

  describe('форматирование без валютного значка (withoutIcon: true)', () => {
    test('должен форматировать без значка с дробной частью', () => {
      expect(formatRubles(1234.56, undefined, true)).toBe('1 234,56');
    });

    test('должен форматировать без значка целое число', () => {
      expect(formatRubles(1000, undefined, true)).toBe('1 000');
    });

    test('должен форматировать без значка отрицательное число', () => {
      expect(formatRubles(-500, undefined, true)).toBe('-500');
    });

    test('должен форматировать без значка ноль', () => {
      expect(formatRubles(0, undefined, true)).toBe('0');
    });

    test('должен сохранять кастомное emptyValue с withoutIcon', () => {
      expect(formatRubles(undefined, 'N/A', true)).toBe('N/A');
    });
  });

  describe('обработка null/undefined/NaN/Infinity', () => {
    test('для null должен вернуть значение по умолчанию "-"', () => {
      expect(formatRubles(null)).toBe('-');
    });

    test('для undefined должен вернуть значение по умолчанию "-"', () => {
      expect(formatRubles(undefined)).toBe('-');
    });

    test('для NaN должен вернуть значение по умолчанию "-"', () => {
      expect(formatRubles(NaN)).toBe('-');
    });

    test('для Infinity должен вернуть значение по умолчанию "-"', () => {
      expect(formatRubles(Infinity)).toBe('-');
    });

    test('для -Infinity должен вернуть значение по умолчанию "-"', () => {
      expect(formatRubles(-Infinity)).toBe('-');
    });
  });

  describe('кастомное значение emptyValue', () => {
    test('для null с пустой строкой должен вернуть пустую строку', () => {
      expect(formatRubles(null, '')).toBe('');
    });

    test('для undefined с "N/A" должен вернуть "N/A"', () => {
      expect(formatRubles(undefined, 'N/A')).toBe('N/A');
    });

    test('для null с "—" должен вернуть "—"', () => {
      expect(formatRubles(null, '—')).toBe('—');
    });

    test('для NaN с "—" должен вернуть "—"', () => {
      expect(formatRubles(NaN, '—')).toBe('—');
    });

    test('для Infinity с "—" должен вернуть "—"', () => {
      expect(formatRubles(Infinity, '—')).toBe('—');
    });
  });
});

describe('formatRublesWithoutPennies', () => {
  describe('форматирование без копеек (с валютным значком)', () => {
    test('должен форматировать число с копейками, убирая их', () => {
      expect(formatRublesWithoutPennies(1234.56)).toBe('1 234 ₽');
      expect(formatRublesWithoutPennies(1000.99)).toBe('1 000 ₽');
    });

    test('должен форматировать целое число', () => {
      expect(formatRublesWithoutPennies(1000)).toBe('1 000 ₽');
    });

    test('должен форматировать отрицательное число', () => {
      expect(formatRublesWithoutPennies(-500)).toBe('-500 ₽');
      expect(formatRublesWithoutPennies(-1234.56)).toBe('-1 234 ₽');
    });

    test('должен форматировать ноль', () => {
      expect(formatRublesWithoutPennies(0)).toBe('0 ₽');
    });
  });

  describe('форматирование без копеек и без валютного значка (withoutIcon: true)', () => {
    test('должен форматировать без значка и без копеек', () => {
      expect(formatRublesWithoutPennies(1234.56, true)).toBe('1 234');
    });

    test('должен форматировать целое число без значка', () => {
      expect(formatRublesWithoutPennies(1000, true)).toBe('1 000');
    });

    test('должен форматировать отрицательное число без значка', () => {
      expect(formatRublesWithoutPennies(-500, true)).toBe('-500');
    });
  });

  describe('обработка 0', () => {
    test('для 0 должен вернуть "0 ₽" (так как emptyValue = "" не используется в formatRublesWithoutPennies)', () => {
      expect(formatRublesWithoutPennies(0)).toBe('0 ₽');
    });
  });
});

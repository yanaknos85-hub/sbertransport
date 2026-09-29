import {
  fromRubles,
  toRubles,
  formatRubles,
  formatRublesWithoutRemainder,
  formatRublesfromRubles
} from '../MoneyUtils';

describe('fromRubles', () => {
  it('умножает рубли на 100', () => {
    expect(fromRubles(1)).toBe(100);
  });

  it('возвращает 0 для 0', () => {
    expect(fromRubles(0)).toBe(0);
  });

  it('работает с дробными рублями', () => {
    expect(fromRubles(2.5)).toBe(250);
  });
});

describe('toRubles', () => {
  it('делит на 100 и округляет (по умолчанию без дробной части)', () => {
    expect(toRubles(100)).toBe(1);
    expect(toRubles(150)).toBe(2);
    expect(toRubles(149)).toBe(1);
  });

  it('возвращает дробную часть при withFraction=true', () => {
    expect(toRubles(1234, true)).toBe(12.34);
    expect(toRubles(150, true)).toBe(1.5);
  });

  it('отбрасывает дробную часть при withFraction=false', () => {
    expect(toRubles(1234, false)).toBe(12);
    expect(toRubles(150, false)).toBe(2);
  });

  it('возвращает 0 для 0', () => {
    expect(toRubles(0)).toBe(0);
    expect(toRubles(0, true)).toBe(0);
  });
});

describe('formatRubles', () => {
  // Intl.NumberFormat использует NO-BREAK SPACE (_) между числом и символом валюты.
  const NBSP = String.fromCharCode(0x00a0);

  it('форматирует число в RUB с двумя знаками', () => {
    // ru-RU: 1\u00A0000,00\u00A0₽ (no-break space между разрядами и перед символом валюты)
    expect(formatRubles(1000)).toBe(`1${NBSP}000,00${NBSP}₽`);
  });

  it('форматирует 0 как "0,00 ₽"', () => {
    expect(formatRubles(0)).toBe(`0,00${NBSP}₽`);
  });

  it('возвращает пустую строку по умолчанию для undefined', () => {
    expect(formatRubles(undefined)).toBe('');
  });

  it('возвращает пустую строку для null', () => {
    expect(formatRubles(null)).toBe('');
  });

  it('возвращает пустую строку для NaN', () => {
    expect(formatRubles(NaN)).toBe('');
  });

  it('возвращает пустую строку для Infinity', () => {
    expect(formatRubles(Infinity)).toBe('');
  });

  it('использует кастомный emptyValue для нечисловых значений', () => {
    expect(formatRubles(undefined, '—')).toBe('—');
    expect(formatRubles(null, 'N/A')).toBe('N/A');
  });
});

describe('formatRublesWithoutRemainder', () => {
  const NBSP = String.fromCharCode(0x00a0);

  it('форматирует число в RUB без копеек', () => {
    // ru-RU с minimumFractionDigits=0: 1\u00A0000\u00A0₽
    expect(formatRublesWithoutRemainder(1000)).toBe(`1${NBSP}000${NBSP}₽`);
  });

  it('возвращает пустую строку по умолчанию для undefined', () => {
    expect(formatRublesWithoutRemainder(undefined)).toBe('');
  });

  it('возвращает пустую строку для null', () => {
    expect(formatRublesWithoutRemainder(null)).toBe('');
  });

  it('возвращает пустую строку для NaN', () => {
    expect(formatRublesWithoutRemainder(NaN)).toBe('');
  });

  it('использует кастомный emptyValue', () => {
    expect(formatRublesWithoutRemainder(undefined, '—')).toBe('—');
  });
});

describe('formatRublesfromRubles', () => {
  it('форматирует рубли как копейки (1 рубль → 100 копеек → формат)', () => {
    expect(formatRublesfromRubles(1)).toBe(formatRubles(100));
  });

  it('возвращает пустую строку для undefined', () => {
    expect(formatRublesfromRubles(undefined)).toBe('');
  });

  it('возвращает пустую строку для null', () => {
    expect(formatRublesfromRubles(null)).toBe('');
  });

  it('возвращает пустую строку для NaN', () => {
    expect(formatRublesfromRubles(NaN)).toBe('');
  });

  it('использует кастомный emptyValue', () => {
    expect(formatRublesfromRubles(undefined, '—')).toBe('—');
  });
});

import { formatDriverRating } from './formatDriverRating';

describe('formatDriverRating', () => {
  test('должен форматировать рейтинг 5.00 (500)', () => {
    expect(formatDriverRating(500)).toBe('5.00');
  });

  test('должен форматировать рейтинг 4.50 (450)', () => {
    expect(formatDriverRating(450)).toBe('4.50');
  });

  test('должен форматировать рейтинг 4.85 (485)', () => {
    expect(formatDriverRating(485)).toBe('4.85');
  });

  test('должен форматировать рейтинг 1.00 (100)', () => {
    expect(formatDriverRating(100)).toBe('1.00');
  });

  test('должен возвращать "-" для рейтинга 0', () => {
    expect(formatDriverRating(0)).toBe('-');
  });

  test('должен возвращать "-" для null', () => {
    expect(formatDriverRating(null)).toBe('-');
  });

  test('должен возвращать "-" для undefined', () => {
    expect(formatDriverRating(undefined)).toBe('-');
  });

  test('должен форматировать дробное значение 3.14 (314)', () => {
    expect(formatDriverRating(314)).toBe('3.14');
  });

  test('должен форматировать большое значение 9.99 (999)', () => {
    expect(formatDriverRating(999)).toBe('9.99');
  });

  test('должен форматировать маленькое значение 0.01 (1)', () => {
    expect(formatDriverRating(1)).toBe('0.01');
  });

  test('должен форматировать рейтинг с округлением 3.141 (314.1)', () => {
    expect(formatDriverRating(314.1)).toBe('3.14');
  });

  test('должен форматировать рейтинг с округлением 3.146 (314.6)', () => {
    expect(formatDriverRating(314.6)).toBe('3.15');
  });

  test('должен форматировать отрицательное значение -1.00 (-100)', () => {
    expect(formatDriverRating(-100)).toBe('-1.00');
    expect(formatDriverRating(-500)).toBe('-5.00');
    expect(formatDriverRating(-999)).toBe('-9.99');
  });

  test('должен возвращать "-" для NaN', () => {
    expect(formatDriverRating(NaN)).toBe('-');
  });
});

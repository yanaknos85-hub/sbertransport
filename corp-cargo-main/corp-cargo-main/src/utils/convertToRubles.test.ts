import { convertToRubles } from './convertToRubles';

describe('convertToRubles', () => {
  test('должен конвертировать целое количество копеек в рубли', () => {
    expect(convertToRubles(100)).toBe(1);
    expect(convertToRubles(200)).toBe(2);
    expect(convertToRubles(1000)).toBe(10);
    expect(convertToRubles(5000)).toBe(50);
  });

  test('должен конвертировать копейки с остатком', () => {
    expect(convertToRubles(150)).toBe(1.5);
    expect(convertToRubles(275)).toBe(2.75);
    expect(convertToRubles(999)).toBe(9.99);
  });

  test('должен возвращать 0 для 0', () => {
    expect(convertToRubles(0)).toBe(0);
  });

  test('должен работать с дробными числами', () => {
    expect(convertToRubles(100.5)).toBe(1.005);
    expect(convertToRubles(200.75)).toBe(2.0075);
  });

  test('должен работать с большими числами', () => {
    expect(convertToRubles(1000000)).toBe(10000);
    expect(convertToRubles(123456789)).toBe(1234567.89);
  });

  test('должен работать с отрицательными числами', () => {
    expect(convertToRubles(-100)).toBe(-1);
    expect(convertToRubles(-500)).toBe(-5);
    expect(convertToRubles(-150)).toBe(-1.5);
  });

  test('должен обрабатывать минимальное значение', () => {
    expect(convertToRubles(1)).toBe(0.01);
  });

  test('должен обрабатывать максимально возможное значение', () => {
    expect(convertToRubles(Number.MAX_SAFE_INTEGER)).toBe(Number.MAX_SAFE_INTEGER / 100);
  });

  test('должен сохранять точность для круглых чисел', () => {
    expect(convertToRubles(50)).toBe(0.5);
    expect(convertToRubles(75)).toBe(0.75);
    expect(convertToRubles(125)).toBe(1.25);
  });
});

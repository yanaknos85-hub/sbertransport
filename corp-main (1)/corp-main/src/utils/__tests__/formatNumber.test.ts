import { formatNumber } from '../formatNumber';

describe('formatNumber', () => {
  it('округляет до 2 знаков для 1.234 (становится 1.23)', () => {
    expect(formatNumber(1.234)).toBe(1.23);
  });

  it('округляет до целого для 1.999 (становится 2)', () => {
    expect(formatNumber(1.999)).toBe(2);
  });

  it('возвращает дробное без лишних нулей для 1.5', () => {
    expect(formatNumber(1.5)).toBe(1.5);
  });

  it('возвращает 1.23 для 1.2345 (округление до 2 знаков)', () => {
    expect(formatNumber(1.2345)).toBe(1.23);
  });

  it('возвращает 0 для 0.001 (округление до 0)', () => {
    expect(formatNumber(0.001)).toBe(0);
  });

  it('возвращает 100 для уже целого числа', () => {
    expect(formatNumber(100)).toBe(100);
  });

  it('возвращает отрицательное дробное как -1.5', () => {
    expect(formatNumber(-1.5)).toBe(-1.5);
  });

  it('округляет отрицательное число до -2 для -1.999', () => {
    expect(formatNumber(-1.999)).toBe(-2);
  });

  it('возвращает 0 для 0', () => {
    expect(formatNumber(0)).toBe(0);
  });

  it('возвращает 1.1 для 1.1 (без изменений)', () => {
    expect(formatNumber(1.1)).toBe(1.1);
  });

  it('возвращает -1.23 для -1.234', () => {
    expect(formatNumber(-1.234)).toBe(-1.23);
  });
});

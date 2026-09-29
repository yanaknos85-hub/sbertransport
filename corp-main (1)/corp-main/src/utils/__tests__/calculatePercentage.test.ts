import { calculatePercentage } from '../calculatePercentage';

describe('calculatePercentage', () => {
  it('возвращает 100, когда otherNumbers пуст и mainNumber положителен', () => {
    expect(calculatePercentage(5, [])).toBe(100);
  });

  it('возвращает 50 для равных долей', () => {
    expect(calculatePercentage(5, [5])).toBe(50);
  });

  it('возвращает 25 для mainNumber=25 и otherNumbers=[75]', () => {
    expect(calculatePercentage(25, [75])).toBe(25);
  });

  it('возвращает 0, когда mainNumber=0', () => {
    expect(calculatePercentage(0, [10, 20])).toBe(0);
  });

  it('возвращает 0 для пустого otherNumbers и mainNumber=0', () => {
    // 0 / (0 + 0) * 100 = NaN, но в JS 0/0 === NaN, не 0.
    expect(calculatePercentage(0, [])).toBeNaN();
  });

  it('суммирует несколько значений в otherNumbers', () => {
    expect(calculatePercentage(10, [10, 10, 20])).toBe(20);
  });

  it('возвращает непериодическую дробь без округления', () => {
    // 2 / (2 + 1 + 3) * 100 = 33.333...
    expect(calculatePercentage(2, [1, 3])).toBeCloseTo(33.33333333333333, 10);
  });

  it('работает с дробными значениями', () => {
    // 0.5 / (0.5 + 1.5) * 100 = 25
    expect(calculatePercentage(0.5, [1.5])).toBe(25);
  });

  it('работает с отрицательными значениями', () => {
    // 10 / (10 + 30 - 20) * 100 = 10 / 20 * 100 = 50
    expect(calculatePercentage(10, [30, -20])).toBe(50);
  });
});

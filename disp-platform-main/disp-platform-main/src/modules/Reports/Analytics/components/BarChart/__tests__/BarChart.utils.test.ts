import { calculateRangeWidth } from '../BarChart.utils';

describe('calculateRangeWidth', () => {
  test('должно корректно вычислять ширину диапазона для значения 3254 и знака "шт" с minRangeWidth = 50', () => {
    const result = calculateRangeWidth({
      maxValue: 3254,
      rangeSign: 'шт',
      minRangeWidth: 50,
    });

    // 4*10 + 2*6 + 6 + 4 = 40 + 12 + 6 + 6 = 64
    expect(result).toBe(64);
  });

  test('должно возвращать minRangeWidth, если вычисленная ширина меньше', () => {
    const result = calculateRangeWidth({
      maxValue: 5,
      rangeSign: 'кг',
      minRangeWidth: 100,
    });

    // 1*10 + 2*6 + 6 + 4 = 10 + 12 + 6 + 6 = 32 → max(34, 100) = 100
    expect(result).toBe(100);
  });

  test('должно корректно обрабатывать однозначное число и односимвольный знак', () => {
    const result = calculateRangeWidth({
      maxValue: 7,
      rangeSign: 'м',
      minRangeWidth: 50,
    });

    // 1*10 + 1*6 + 6 + 4 = 10 + 6 + 6 + 6 = 26 → max(28, 50) = 50
    expect(result).toBe(50);
  });

  test('должно корректно обрабатывать большое число', () => {
    const result = calculateRangeWidth({
      maxValue: 123456789,
      rangeSign: 'шт',
      minRangeWidth: 50,
    });

    // 9*10 + 2*6 + 6 + 4 = 90 + 12 + 6 + 6 = 114
    expect(result).toBe(114);
  });
});

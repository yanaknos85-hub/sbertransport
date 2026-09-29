import { getWaypointName } from '../getWaypointName';

describe('getWaypointName', () => {
  test('должен возвращать "Начало поездки" для индекса 0', () => {
    expect(getWaypointName(0, 5)).toBe('Начало поездки');
  });

  test('должен возвращать "Конец поездки" для последнего индекса', () => {
    expect(getWaypointName(4, 5)).toBe('Конец поездки');
  });

  test('должен возвращать "Промежуточная точка" для промежуточных индексов', () => {
    expect(getWaypointName(1, 5)).toBe('Промежуточная точка');
    expect(getWaypointName(2, 5)).toBe('Промежуточная точка');
    expect(getWaypointName(3, 5)).toBe('Промежуточная точка');
  });

  test('должен корректно работать при длине 1 (начало и конец совпадают)', () => {
    expect(getWaypointName(0, 1)).toBe('Начало поездки');
  });
});

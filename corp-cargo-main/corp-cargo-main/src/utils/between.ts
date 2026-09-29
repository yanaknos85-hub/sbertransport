/**
 * Проверяет, находится ли число в определенном диапазоне (включительно)
 * @param x
 * @param min
 * @param max
 */
export const inRange = (min: number, max: number) => (x: number): boolean => x >= min && x <= max;

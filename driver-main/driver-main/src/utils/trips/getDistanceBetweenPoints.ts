type Point = [number, number];
/**
 * Вычисляет расстояние между двумя точками на Земле (в километрах).
 * @param {Point} - [longitude, latitude] первой точки (в градусах)
 * @param {Point} - [longitude, latitude] второй точки (в градусах)
 * @returns {number} Расстояние в километрах
 */
export function getDistanceBetweenPoints(
  [lon1, lat1]: Point,
  [lon2, lat2]: Point
): number {
  const R = 6371; // Радиус Земли в километрах

  // Переводим градусы в радианы
  const toRad = (deg: number) => deg * (Math.PI / 180);

  const φ1 = toRad(lat1); // Широта точки 1
  const φ2 = toRad(lat2); // Широта точки 2
  const Δφ = toRad(lat2 - lat1); // Разница широт
  const Δλ = toRad(lon2 - lon1); // Разница долгот

  // Формула Хаверсина
  const a = Math.sin(Δφ / 2) ** 2
    + Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ / 2) ** 2;
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  return R * c; // Расстояние в километрах
}

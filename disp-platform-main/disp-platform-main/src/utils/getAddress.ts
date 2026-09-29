import { Waypoint } from 'api/trips/trips.types';

/**
 * Составляет полный адрес из компонентов путевой точки
 * @param waypoint - объект путевой точки с полями country, region, city, street, house, building
 * @returns строка с полным адресом, сформированная из доступных компонентов
 */
export const getAddress = (waypoint: Waypoint): string => (
  [waypoint.country, waypoint.region, waypoint.city, waypoint.street, waypoint.house, waypoint.building]
    .filter(Boolean)
    .join(', ')
);

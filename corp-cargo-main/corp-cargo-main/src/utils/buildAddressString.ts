import { Waypoint } from '../stores/Geo/Geo.interface';

export const buildAddressString = (waypoint: Waypoint): string => (
  `${waypoint.street ? `${waypoint.street}, ` : ''}`
  + `${waypoint.house ? `${waypoint.house}, ` : ''}`
  + `${waypoint.city ? `${waypoint.city},` : ''}`
).slice(0, -1);

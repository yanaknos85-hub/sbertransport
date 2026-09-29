import { Waypoint } from 'api/trips-cargo/trips-cargo.types';

export const getAddress = (waypoint: Waypoint) => (
  [waypoint.country, waypoint.region, waypoint.city, waypoint.street, waypoint.house, waypoint.building]
    .filter(Boolean)
    .join(', ')
);

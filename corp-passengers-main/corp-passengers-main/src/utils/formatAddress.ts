import { Waypoint } from 'stores/Geo/Geo.interface';
import { LocationAddress } from 'stores/Locations/Locations.interface';
import { WaypointsInfo } from 'stores/PublicRegistry/models/PublicRegistry.interface';
import { Waypoints } from '../stores/PersonalSearch/PersonalSearch.interface';
import { TaxiWaypoint, TripStop } from '../stores/TaxiRegistry/models/TaxiRegistry.interface';

export const formatAddress = (
  point?: Waypoint | LocationAddress | WaypointsInfo | TaxiWaypoint | null
): string => {
  if (!point) {
    return '';
  }
  return [point.street, point.house, point.city].filter(x => x).join(', ');
};

export const formatAddressForRequest = (point?: string): string => {
  if (!point) {
    return '';
  }
  return point.split(',').join('');
};

export const formatWaypoints = (
  points: Waypoints[] | WaypointsInfo[] | TaxiWaypoint[] | null | undefined,
  emptyValue = '-'
): string => {
  if (!points) {
    return emptyValue;
  }

  const intermediateAddresses = points.slice(1, -1);
  return intermediateAddresses.length
    ? intermediateAddresses.map((p: Waypoints | WaypointsInfo) => formatAddress(p)).join(' /\n')
    : emptyValue;
};

export const formatWaypointsCoopTrip = (points: TripStop[] | null | undefined): string => {
  if (!points) {
    return '-';
  }
  const intermediateAddresses = points.slice(1, -1);
  return intermediateAddresses.length
    ? intermediateAddresses.map((p: TripStop) => formatAddress(p.waypoint)).join(' /\n')
    : '-';
};

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const formatFullRoute = (points: Waypoints[] | WaypointsInfo[] | undefined) => points && points.length ? (points as any).map((p: Waypoints | WaypointsInfo) => formatAddress(p)).join(' / ') : '-';

// TODO: временное решение, пока бэк не возвращается адрес в необходимом виде
export const transformAddress = (a: string): string => {
  const parts: string[] = a.split(', ');
  if (parts.length >= 4) {
    parts.splice(0, 2);
  }
  return parts.join(', ');
};

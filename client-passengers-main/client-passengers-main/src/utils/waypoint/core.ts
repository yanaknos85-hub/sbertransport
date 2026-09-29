import { TWaypoint } from 'shared/models/geo/types';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';

export const buildAddressString = (waypoint: TWaypoint): string => {
  const {
    street, house, city,
  } = waypoint;
  return `${street ? `${street}, ` : ''}${house ? `${house}, ` : ''}${city ? `${city},` : ''}`.slice(0, -1);
};

export const waypointIsValid = (waypoint: TWaypoint): boolean => !!waypoint.latitude && !!waypoint.longitude;

export const MIN_WAYPOINT_LENGTH = 2;

export const REQUIRED_WAYPOINT_KEYS = [
  'addressString',
  'longitude',
  'latitude',
  'city',
  'country',
  'house',
  'street',
] as (keyof WaypointModel)[];

export const twoWaypointsIsSimilarCore = (
  firstWaypoint: WaypointModel | undefined,
  secondWaypoint: WaypointModel | undefined,
  keys: (keyof WaypointModel)[]
): boolean => {
  if (!firstWaypoint || !secondWaypoint) {
    return false;
  }

  let result = true;

  keys.forEach(key => {
    if (firstWaypoint[key as keyof WaypointModel] !== secondWaypoint[key as keyof WaypointModel]) {
      result = false;
    }
  });

  return result;
};

export const checkWaypointsArraysSimilarityCore = (
  arr1: WaypointModel[] | undefined,
  arr2: WaypointModel[] | undefined,
  keys: (keyof WaypointModel)[]
): boolean => {
  if (!arr1 || !arr2 || arr1.length !== arr2.length) {
    return false;
  }

  let result = true;

  arr1.forEach((el, index) => {
    if (!twoWaypointsIsSimilarCore(el, arr2[index], keys)) {
      result = false;
    }
  });

  return result;
};

export const allWaypointsIsValid = (waypoints?: WaypointModel[]): boolean => !!waypoints?.every(_ => _.isValid);

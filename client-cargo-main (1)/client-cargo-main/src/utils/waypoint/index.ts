import { WaypointModel } from 'shared/models/geo/Waypoint.model';

import { checkWaypointsArraysSimilarityCore, REQUIRED_WAYPOINT_KEYS, twoWaypointsIsSimilarCore } from './core';

export const twoWaypointsIsSimilar = (prev: WaypointModel | undefined, cur: WaypointModel | undefined): boolean => {
  const keys = REQUIRED_WAYPOINT_KEYS;

  return twoWaypointsIsSimilarCore(prev, cur, keys);
};

export const waypointsLocationIsSimilar = (
  prev: WaypointModel[] | undefined,
  cur: WaypointModel[] | undefined
): boolean => {
  const keys = REQUIRED_WAYPOINT_KEYS;

  return checkWaypointsArraysSimilarityCore(prev, cur, keys);
};

export const waypointsArraysOrderIsSimilar = (
  prev: WaypointModel[] | undefined,
  cur: WaypointModel[] | undefined
): boolean => {
  const keys = ['addressString', 'longitude', 'latitude'] as (keyof WaypointModel)[];
  return checkWaypointsArraysSimilarityCore(prev, cur, keys);
};

export const allWaypointsIsValid = (waypoints?: WaypointModel[]): boolean => !!waypoints?.every(_ => _.isValid);

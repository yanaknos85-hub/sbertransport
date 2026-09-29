import { plainToNew } from 'utils';

import { getDistanceString, getTimeString } from 'utils/Misc';

import { RequestRoute, Segment } from './types';
import { WaypointModel } from './Waypoint.model';

export class RouteModel implements RequestRoute {
  distance = 0;

  time = 0;

  segments: Segment[] = [];

  waypoints: WaypointModel[] = [];

  cost?: number;

  constructor(route?: RequestRoute) {
    if (route) {
      this.distance = route.distance;
      this.time = route.time;
      this.segments = route.segments;
      this.waypoints = plainToNew(WaypointModel, route.waypoints) ?? [];
      this.cost = route.cost;
    }
  }

  getTimeString(): string {
    return getTimeString(this.time);
  }

  getDistanceString(): string {
    return getDistanceString(this.distance);
  }

  setCost(value: number): void {
    this.cost = value;
  }
}

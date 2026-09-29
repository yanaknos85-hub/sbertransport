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

  outcomeCost?: number;

  constructor(route?: RequestRoute) {
    if (route) {
      this.distance = route.distance;
      this.time = route.time;
      this.segments = route.segments;
      this.waypoints = plainToNew(WaypointModel, route.waypoints) ?? [];
      this.cost = route.cost;
      this.outcomeCost = route.outcomeCost;
    }
  }

  getTimeString(): string {
    const waitTime = this.waypoints.reduce((acc, value) => acc + value.waitTime, 0);

    return getTimeString(this.time + waitTime);
  }

  getDistanceString(): string {
    return getDistanceString(this.distance);
  }

  setCost(value: number): void {
    this.cost = value;
  }
}

import { CheckinInfo, PassTrip } from 'api/trips/trips.types';
import { WaypointWithType } from 'types/waypoint';

export interface ITripInfo {
  /** Информация о поездке */
  trip: PassTrip;
  /** Информаця о чекинах */
  checkinInfo: CheckinInfo;
  /** Информация о точках маршрута */
  waypoints: WaypointWithType[];
}

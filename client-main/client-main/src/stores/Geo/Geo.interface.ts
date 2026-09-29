import { RouteModel } from 'shared/models/geo/Route.model';
import { LatLngTuple, RequestRoute, TWaypoint } from 'shared/models/geo/types';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';
import { AddressModel } from 'stores/Address/models/Address.model';

export interface IGeoStore {
  currentCoordinates: LatLngTuple;
  addressAutocompleteList: WaypointModel[];
  waypoints: WaypointModel[];
  calculatedRoute: RouteModel | undefined;
  setCurrentAddressByCoordinates(coordinates: LatLngTuple): Promise<void>;
  searchLocation(val: string): void;
  editWaypointAddress(index: number, value: string, centerCoordinates?: LatLngTuple): void;
  editWaypointWaitTime(index: number, time: string): void;
  editSingleAddress(value: string, centerCoordinates?: LatLngTuple): void;
  addWaypoint(): void;
  removeWaypoint(index: number): void;
  onAddressSelect(value: string, options: any, index: number, dontClear?: boolean): void;
  clearCurrentState(): void;
  setWaypointFromAddress(address: AddressModel, currentInputNumber: number): void;
  onAddressBySortCoordinates(coordinates: LatLngTuple): Promise<TWaypoint[]>;
}

export interface IGeoService {
  getAddressByCoordinates(coordinates: LatLngTuple): Promise<TWaypoint[]>;
  getCoordinateByAddress(location: string, centerCoordinates?: LatLngTuple): Promise<TWaypoint[]>;
  calcRoute(route: WaypointModel[]): Promise<RequestRoute>;
  getAddressBySortCoordinates(coordinates: LatLngTuple): Promise<TWaypoint[]>;
}

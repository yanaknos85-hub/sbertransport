import { RouteModel } from 'shared/models/geo/Route.model';
import {
  LatLngTuple, RequestRoute, TWaypoint, TWaypointVSP
} from 'shared/models/geo/types';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';

import { AddressModel } from 'stores/Address/models/Address.model';
import { TripRequestModel } from 'stores/Trip/models';

import { RequestModel as CargoMassRequestModel } from '../CargoMassMultiple/models/CargoMassRequestMultiple.model';

export interface IGeoStore {
  currentCoordinates: LatLngTuple;
  addressAutocompleteList: WaypointModel[];
  waypoints: WaypointModel[];
  calculatedRoute: RouteModel | undefined;
  loadFromTripRequest(trip: TripRequestModel): void;
  loadFromCargoMassRequestMulti(cargo: CargoMassRequestModel): void;
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
  addCalculatedRoute: (waypoints: any) => Promise<void>;
}

export interface IGeoService {
  getAddressByCoordinates(coordinates: LatLngTuple): Promise<TWaypoint[]>;
  getCoordinateByAddress(location: string, centerCoordinates?: LatLngTuple): Promise<TWaypoint[]>;
  getCoordinateByVspAddress(location: string, centerCoordinates?: LatLngTuple): Promise<TWaypointVSP[]>;
  calcRoute(route: WaypointModel[]): Promise<RequestRoute>;
  getAddressBySortCoordinates(coordinates: LatLngTuple): Promise<TWaypoint[]>;
}

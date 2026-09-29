/* eslint-disable no-underscore-dangle */
import { inject, injectable } from 'inversify';
import debounce from 'lodash/debounce';
import mapKeys from 'lodash/mapKeys';
import { action, observable } from 'mobx';

import { TYPES } from 'ioc/types';
import { SYSTEM_MESSAGES } from 'constants/constants.app';
import type { ILogger } from '@sber-sbertransport/mf-core';

import { RouteModel } from 'shared/models/geo/Route.model';
import type { LatLngTuple } from 'shared/models/geo/types';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';
import { AddressModel } from 'stores/Address/models/Address.model';
import { TripRequestModel } from 'stores/Trip/models';
import { plainToNew } from 'utils';

import type { IGeoService, IGeoStore } from './Geo.interface';

import { MoscowLatLng } from 'modules/EmployeeApp/EmployeeApp.constants';

@injectable()
export class DIGeoStore implements IGeoStore {
  @inject(TYPES.IGeoService)
  private service!: IGeoService;

  @observable
    currentCoordinates: LatLngTuple = MoscowLatLng;

  @observable
    addressAutocompleteList: WaypointModel[] = [];

  @observable
    waypoints: WaypointModel[] = [new WaypointModel(), new WaypointModel()];

  @observable
    calculatedRoute: RouteModel | undefined = undefined;

  @action.bound
  setCurrentCoordinates(coords: LatLngTuple) {
    this.currentCoordinates = coords;
  }

  @action.bound
  async setCurrentAddressByCoordinates(coordinates: LatLngTuple): Promise<void> {
    this.setCurrentCoordinates(coordinates);

    const point = await this.getAddressByCoordinates(coordinates);
    this.editWaypoints(0, point[0]);
    this.calcRoute();
  }

  @action.bound
  setWaypointFromAddress(address: AddressModel, currentInputNumber: number): void {
    this.editWaypoints(currentInputNumber, address);
    this.calcRoute();
  }

  @action.bound
    searchLocation = debounce(async (location: string, centerCoordinates?: LatLngTuple) => {
      this.addressAutocompleteList = observable(await this.getCoordinateByAddress(location, centerCoordinates));
    }, 300);

  @action.bound
  editWaypointAddress(index: number, value: string, centerCoordinates?: LatLngTuple): void {
    this.waypoints[index].addressString = value;

    if (!value) {
      this.clearAutocompleteList();
      this.editWaypoints(index, new WaypointModel());
      this.calcRoute();
    }
    if (value?.length > 3 && !this.addressAutocompleteList.some(x => x.addressString === value)) {
      this.searchLocation(value, centerCoordinates);
    }
  }

  @action.bound
  editWaypointWaitTime(index: number, time: string): void {
    this.waypoints[index].waitTimeMinutes = Number(time);
    this.calcRoute();
  }

  @action.bound
  editSingleAddress(value: string, centerCoordinates?: LatLngTuple): void {
    if (value.length > 3 && !this.addressAutocompleteList.some(x => x.addressString === value)) {
      this.clearAutocompleteList();
      this.searchLocation(value, centerCoordinates);
    }
  }

  @action.bound
  async onAddressBySortCoordinates(coordinates: LatLngTuple): Promise<WaypointModel[]> {
    const data = await this.service.getAddressBySortCoordinates(coordinates);
    return plainToNew<WaypointModel[]>(WaypointModel, data) ?? [];
  }

  @action.bound
  async onAddressSelect(value: string, _: unknown, index: number, dontClear?: boolean): Promise<void> {
    const point = this.addressAutocompleteList.find(x => x.addressString === value);
    if (point) {
      this.editWaypoints(index, point);
    }
    if (!dontClear) {
      this.clearAutocompleteList();
    }
    this.calcRoute();
  }

  private calcRoute = debounce(async (): Promise<void> => {
    if (this.waypoints.every(x => x.isValid)) {
      const data = await this.service.calcRoute(this.waypoints);
      this.calculatedRoute = new RouteModel(data);
    } else {
      this.clearRoute();
    }
  }, 100);

  @action.bound
  addWaypoint(): void {
    this._addWaypoint();
    this.clearRoute();
  }

  @action.bound
  removeWaypoint(index: number): void {
    this._removeWaypoint(index);
    this.calcRoute();
  }

  @action.bound
    addExistingWaypoint = async (existingWaypoint: TripRequestModel): Promise<void> => {
      this.waypoints = existingWaypoint.expected.waypoints;
      const data = await this.service.calcRoute(existingWaypoint.expected.waypoints);
      this.calculatedRoute = new RouteModel(data);
    };

  clearCurrentState(): void {
    this.setCurrentCoordinates(MoscowLatLng);
    this.clearAutocompleteList();
    this.waypoints = [new WaypointModel(), new WaypointModel()];
    this.clearRoute();
  }

  @action.bound
  loadFromTripRequest(trip: TripRequestModel): void {
    this.waypoints = [...trip.expected.waypoints];
    this.calculatedRoute = trip.expected;
  }

  @action.bound
  async onMapClick(latitude: number, longitude: number, logger: ILogger): Promise<void> {
    const index = this.waypoints.findIndex(item => item.country === undefined);
    const resultIndex = index !== -1 ? index : this.waypoints.length - 1;
    this.editWaypoints(resultIndex, new WaypointModel());
    const points = await this.getAddressByCoordinates([latitude, longitude], 50)
      .catch(() => {
        logger.toMessage('error', SYSTEM_MESSAGES.errorOccurred);
      });

    this.editWaypoints(resultIndex, points?.[0] ?? new WaypointModel());
    this.calcRoute();
  }

  private async getAddressByCoordinates(coordinates: LatLngTuple, radius?: number): Promise<WaypointModel[]> {
    const data = await this.service.getAddressByCoordinates(coordinates, radius);
    return plainToNew<WaypointModel[]>(WaypointModel, data) ?? [];
  }

  private async getCoordinateByAddress(location: string, centerCoordinates?: LatLngTuple): Promise<WaypointModel[]> {
    const data = await this.service.getCoordinateByAddress(location, centerCoordinates);
    const models = plainToNew<WaypointModel[]>(WaypointModel, data) ?? [];
    const noBlank = models.filter(x => x.addressString !== '');
    return Object.values(mapKeys(noBlank, 'addressString')); // removes duplicates
  }

  private editWaypoints(index: number, value: WaypointModel): void {
    this.waypoints[index] = value;
    this.waypoints = [...this.waypoints.slice()];
  }

  private clearAutocompleteList(): void {
    this.addressAutocompleteList = [];
  }

  private clearRoute(): void {
    this.calculatedRoute = undefined;
  }

  private _addWaypoint(): void {
    this.waypoints = [...this.waypoints, new WaypointModel()];
  }

  private _removeWaypoint(index: number): void {
    const array = [...this.waypoints.filter((x, idx) => idx !== index)];
    if (array.length === 2) {
      array[1].waitTimeMinutes = 0;
    }
    this.waypoints = array;
  }
}

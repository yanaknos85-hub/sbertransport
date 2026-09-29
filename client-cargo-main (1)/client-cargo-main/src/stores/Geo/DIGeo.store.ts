import { inject, injectable } from 'inversify';
import { TYPES } from 'ioc/types';
import debounce from 'lodash/debounce';
import mapKeys from 'lodash/mapKeys';
import { action, observable } from 'mobx';
import { validationPatterns } from 'shared/fieldValidationRules';
import { RouteModel } from 'shared/models/geo/Route.model';
import type { LatLngTuple, TWaypoint, TWaypointVSP } from 'shared/models/geo/types';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';
import { plainToNew } from 'utils';

import { AddressModel } from 'stores/Address/models/Address.model';
import { RequestModel as CargoMassRequestModel } from 'stores/CargoMassMultiple/models/CargoMassRequestMultiple.model';
import { CargoRequestModel } from 'stores/Cargos/models/CargoRequest.model';
import { TripRequestModel } from 'stores/Trip/models';

import type { IAddressStore } from '../Address/Address.interface';
import type { IGeoService, IGeoStore } from './Geo.interface';

// Moscow coordinates
const defaultMapCoords: LatLngTuple = [55.752693, 37.617423];

@injectable()
export class DIGeoStore implements IGeoStore {
  @inject(TYPES.IGeoService)
  private service!: IGeoService;

  @inject(TYPES.IAddressStore)
  private address!: IAddressStore;

  @observable
    currentCoordinates: LatLngTuple = defaultMapCoords;

  @observable
    addressAutocompleteList: WaypointModel[] = [];

  @observable
    waypoints: WaypointModel[] = [new WaypointModel(), new WaypointModel()];

  @observable
    calculatedRoute: RouteModel | undefined = undefined;

  @action.bound
  async setCurrentAddressByCoordinates(coordinates: LatLngTuple): Promise<void> {
    this.currentCoordinates = coordinates;
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
    }, 1500);

  @action.bound
    searchLocationVSP = debounce(async (location: string, centerCoordinates?: LatLngTuple) => {
      this.addressAutocompleteList = observable(await this.getCoordinateByVspAddress(location, centerCoordinates));
    }, 1500);

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
    const isVSP = validationPatterns.vspValidation.test(value);
    const isVSPFull = validationPatterns.vspValidationFull.test(value);

    if (value.length > 3 && !this.addressAutocompleteList.some(x => x.addressString === value)) {
      if (isVSP || isVSPFull) {
        this.clearAutocompleteList();
        this.searchLocationVSP(value, centerCoordinates);
      } else {
        this.clearAutocompleteList();
        this.searchLocation(value, centerCoordinates);
      }
    }
  }

  @action.bound
  async onAddressBySortCoordinates(coordinates: LatLngTuple): Promise<WaypointModel[]> {
    const data = await this.service.getAddressBySortCoordinates(coordinates);
    return plainToNew<WaypointModel[]>(WaypointModel, data) ?? [];
  }

  @action.bound
  async onAddressSelect(value: string, _: unknown, index: number, dontClear?: boolean): Promise<void> {
    const point = [
      ...this.addressAutocompleteList,
      ...this.address.selfFavoriteList,
      ...this.address.selfAddressList,
      ...this.address.selfFrequentList,
    ].find(x => x.addressString === value);

    if (point) {
      this.editWaypoints(index, point);
    }
    if (!dontClear) {
      this.clearAutocompleteList();
    }
    this.calcRoute();
  }

  private waypointIsValid = (waypoint: TWaypoint): boolean => {
    return !!waypoint.latitude && !!waypoint.longitude;
  };

  private calcRoute = debounce(async (): Promise<void> => {
    this.clearRoute();
    if (this.waypoints.every(waypoint => {
      return this.waypointIsValid(waypoint);
    })) {
      const data = await this.service.calcRoute(this.waypoints);
      this.calculatedRoute = new RouteModel({ ...data, waypoints: this.waypoints });
    }
  }, 1000);

  @action.bound
  addWaypoint(): void {
    this._addWaypoint();
    this.clearRoute();
  }

  @action.bound
    addCalculatedRoute = async (waypoints: any): Promise<void> => {
      this.waypoints = waypoints;
      const data = await this.service.calcRoute(waypoints);
      this.calculatedRoute = new RouteModel(data);
    };

  @action.bound
    fillForSteps = (cargoMultipleRequest: CargoRequestModel): void => {
      this.waypoints = cargoMultipleRequest.waypoints;
      this.calculatedRoute = new RouteModel ({
        distance: cargoMultipleRequest.segments.reduce((acc, segment) => acc + segment.distance, 0),
        time: cargoMultipleRequest.segments.reduce((acc, segment) => acc + segment.time, 0),
        segments: cargoMultipleRequest.segments,
        waypoints: cargoMultipleRequest.waypoints,
      });
    };

  @action.bound
  removeWaypoint(index: number): void {
    this._removeWaypoint(index);
    this.calcRoute();
  }

  clearCurrentState(): void {
    this.currentCoordinates = defaultMapCoords;
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
  loadFromCargoMassRequestMulti(cargo: CargoMassRequestModel): void {
    this.waypoints = [...cargo.expected.waypoints];
    // todo доработать
    //  очистка calculatedRoute, чтобы избежать вызова сервиса calculated при монтировании компонента
    this.clearRoute();
  }

  private async getAddressByCoordinates(coordinates: LatLngTuple): Promise<WaypointModel[]> {
    const data = await this.service.getAddressByCoordinates(coordinates);
    return plainToNew<WaypointModel[]>(WaypointModel, data) ?? [];
  }

  private async getCoordinateByAddress(location: string, centerCoordinates?: LatLngTuple): Promise<WaypointModel[]> {
    const data = await this.service.getCoordinateByAddress(location, centerCoordinates);
    const models = plainToNew<WaypointModel[]>(WaypointModel, data) ?? [];
    const noBlank = models.filter(x => x.addressString !== '');
    return Object.values(mapKeys(noBlank, 'addressString')); // removes duplicates
  }

  private async getCoordinateByVspAddress(location: string, centerCoordinates?: LatLngTuple): Promise<any[]> {
    const data = await this.service.getCoordinateByVspAddress(location, centerCoordinates);
    const modelsForPlain = DIGeoStore.serializeVspToWaypointModel(data);
    const models = plainToNew<WaypointModel[]>(WaypointModel, modelsForPlain) ?? [];
    const noBlank = models.filter(x => x.addressString !== '');
    return Object.values(mapKeys(noBlank, 'addressString')); // removes duplicates
  }

  private editWaypoints(index: number, value: WaypointModel): void {
    this.waypoints[index] = value;
    this.waypoints = [...this.waypoints.slice()];
  }

  private static serializeVspToWaypointModel(data: TWaypointVSP[]): TWaypoint[] {
    return data.map(entry => {
      const { kic } = entry;
      const addressType = kic ? 'VSP_KIC' : 'VSP';
      const kicData = { ...kic?.address, code: kic?.code };
      return {
        ...entry.address, gosb: entry.gosb, kic: kicData, name: entry.name, addressType,
      };
    });
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

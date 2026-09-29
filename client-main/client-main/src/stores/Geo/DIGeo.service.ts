import type { IHttpService, IResponseService } from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';

import {
  CALC_ROUTE,
  GET_ADDRESS_BY_COORDINATES,
  GET_COORDINATES_BY_ADDRESS,
  MOCKED_API_PREFIX
} from 'constants/constants.env';

import { TYPES } from 'ioc/types';

import { RouteModel } from 'shared/models/geo/Route.model';
import { LatLngTuple, RequestRoute, TWaypoint } from 'shared/models/geo/types';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';

import { IGeoService } from './Geo.interface';

@injectable()
export class DIGeoService implements IGeoService {
  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.IResponseService)
  private process!: IResponseService;

  async getAddressByCoordinates(coordinates: LatLngTuple): Promise<TWaypoint[]> {
    const latitude = coordinates[0].toFixed(6);
    const longitude = coordinates[1].toFixed(6);

    return this.http
      .get<WaypointModel[]>(`${GET_ADDRESS_BY_COORDINATES}`, { params: { latitude, longitude } })
      .then(this.process.getResponseData);
  }

  async getAddressBySortCoordinates(coordinates: LatLngTuple): Promise<TWaypoint[]> {
    const latitude = coordinates[0].toFixed(6);
    const longitude = coordinates[1].toFixed(6);

    return this.http
      .get<WaypointModel[]>(`${GET_ADDRESS_BY_COORDINATES}`, {
        params: {
          sortLatitude: latitude,
          sortLongitude: longitude,
        },
      })
      .then(this.process.getResponseData);
  }

  async getCoordinateByAddress(location: string, centerCoordinates?: LatLngTuple): Promise<TWaypoint[]> {
    return this.http
      .get<WaypointModel[]>(`${MOCKED_API_PREFIX}${GET_COORDINATES_BY_ADDRESS}`, {
        params: {
          location,
          centerLatitude: centerCoordinates?.[0],
          centerLongitude: centerCoordinates?.[1],
        },
      })
      .then(this.process.getResponseData);
  }

  async calcRoute(route: WaypointModel[]): Promise<RequestRoute> {
    return this.http
      .post<RouteModel>(`${MOCKED_API_PREFIX}${CALC_ROUTE}`, { coordinates: route })
      .then(x => this.process.getResponseData(x, RequestRoute));
  }
}

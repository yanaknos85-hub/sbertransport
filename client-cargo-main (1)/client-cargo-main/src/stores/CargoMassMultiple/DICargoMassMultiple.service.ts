import type { IHttpService, IResponseService } from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';
import { TYPES } from 'ioc/types';
import { RouteModel } from 'shared/models/geo/Route.model';
import { RequestRoute } from 'shared/models/geo/types';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';

import { RequestObject } from 'types/Cargo';
import {
  ADD_CARGO_MASS_MULTIPLE,
  ADD_REGULAR_CARGO_MASS_MULTIPLE,
  CALC_ROUTE,
  GET_MASS_REGULAR_REQUEST_MULTIPLE,
  GET_MASS_REQUEST_MULTI,
  MASS_REQUEST_SAVE_FILE_MULTIPLE,
  MASS_REQUEST_SAVE_FILE_REGULAR,
  MOCKED_API_PREFIX
} from 'constants/constants.api';
import { RequestObjectMulti } from 'modules/CargoMassMultiple/types';

import { ICargoMassServiceMultiple, RequestServer, SavedFileInfo } from './CargoMassMultiple.interface';

@injectable()
export class DICargoMassServiceMultiple implements ICargoMassServiceMultiple {
  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.IResponseService)
  private process!: IResponseService;

  getRequest(id: string): Promise<any> {
    return this.http
      .get<RequestServer>(`${MOCKED_API_PREFIX}${GET_MASS_REQUEST_MULTI}/${id}`)
      .then(this.process.getResponseData);
  }

  async saveFile(file: File): Promise<SavedFileInfo | void> {
    const formData = new FormData();
    formData.append('file', file);
    try {
      return await this.http
        .post<SavedFileInfo>(`${MOCKED_API_PREFIX}${MASS_REQUEST_SAVE_FILE_MULTIPLE}`, formData)
        .then(x => this.process.getResponseData(x, SavedFileInfo));
    } catch (err: any) {
      throw new Error(err?.response?.data?.message);
    }
  }

  async saveFileRegular(file: File): Promise<SavedFileInfo | void> {
    const formData = new FormData();
    formData.append('file', file);
    try {
      return await this.http
        .post<SavedFileInfo>(`${MOCKED_API_PREFIX}${MASS_REQUEST_SAVE_FILE_REGULAR}`, formData)
        .then(x => this.process.getResponseData(x, SavedFileInfo));
    } catch (err: any) {
      throw new Error(err?.response?.data?.message);
    }
  }

  calcRoute(route: WaypointModel[]): Promise<RequestRoute> {
    return this.http
      .post<RouteModel>(`${MOCKED_API_PREFIX}${CALC_ROUTE}`, { coordinates: route })
      .then(data => this.process.getResponseData(data, RequestRoute));
  }

  async postData(data: RequestObjectMulti[]): Promise<any> {
    return this.http.post<any>(`${ADD_CARGO_MASS_MULTIPLE}`, [...data]).then(this.process.getResponseStatus);
  }

  async postRegularData(data: RequestObject[]): Promise<any> {
    return this.http.post<any>(`${ADD_REGULAR_CARGO_MASS_MULTIPLE}`, [...data])
      .then(data => this.process.getResponseData(data, SavedFileInfo));
  }

  getRegularRequest(id: string): Promise<any> {
    return this.http
      .get<RequestServer>(`${MOCKED_API_PREFIX}${GET_MASS_REGULAR_REQUEST_MULTIPLE}/${id}`)
      .then(this.process.getResponseData);
  }
}

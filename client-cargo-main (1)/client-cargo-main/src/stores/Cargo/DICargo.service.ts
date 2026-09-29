import type { IHttpService, IResponseService } from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';
import { TYPES } from 'ioc/types';

import {
  BackendCargoPersonalListItemType,
  CargoPersonalListItemType,
  RequestResponseSingleOrder
} from 'types/Cargo';
import {
  ADD_CARGO_MULTI,
  ADD_REGULAR_CARGO_MULTI,
  CREATE_PERSONAL_CARGO_LIST,
  GET_PACK_CARGO,
  GET_REGULAR_COST_MULTIPLE
} from 'constants/constants.api';

import { CargoRegularRequestResponse, PackData, PeriodType } from '../Cargos/types';
import { OrderRequestMulti, OrderRequestRegularMulti } from '../Cargos/typesMulti';
import { ICargoService } from './Cargo.interface';

@injectable()
export class DICargoService implements ICargoService {
  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.IResponseService)
  private process!: IResponseService;

  async postDataMulti(data: OrderRequestMulti): Promise<RequestResponseSingleOrder> {
    return this.http
      .post<RequestResponseSingleOrder>(`${ADD_CARGO_MULTI}`, {
        ...data,
      })
      .then(response => response.data);
  }

  async postRegularCargoDataMulti(data: OrderRequestRegularMulti): Promise<CargoRegularRequestResponse> {
    return this.http
      .post<CargoRegularRequestResponse>(`${ADD_REGULAR_CARGO_MULTI}`, {
        ...data,
      })
      .then(response => response.data);
  }

  async getPeriodValuesMulti(data: Partial<PeriodType>): Promise<any> {
    return this.http
      .get<Partial<PeriodType>>(`${GET_REGULAR_COST_MULTIPLE}`, {
        urlParams: {
          dayOfWeek: data.dayOfWeek?.join(',') as string,
          periodType: data.periodType as string,
          weekOfMonth: data.weekOfMonth?.join(',') as string,
          monthOfQuartal: data.monthOfQuartal?.join(',') as string,
          beginDate: data.beginDate as string,
          endDate: data.endDate as string,
          cost: data.cost as any,
        },
      })
      .then(this.process.getResponseData);
  }

  async getPack(): Promise<PackData> {
    return this.http
      .get<PackData>(`${GET_PACK_CARGO}`)
      .then(this.process.getResponseData);
  }

  async createCargoItemPersonal(data: CargoPersonalListItemType): Promise<BackendCargoPersonalListItemType> {
    return this.http
      .post<BackendCargoPersonalListItemType>(`${CREATE_PERSONAL_CARGO_LIST}`, { ...data })
      .then(this.process.getResponseData);
  }
}


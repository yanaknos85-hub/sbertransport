import type { IHttpService, IResponseService } from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';

import {
  DELETE_PERSONAL_CARGO_LIST,
  GET_PERSONAL_CARGO_LIST
} from 'constants/constants.env';
import { PageSetting } from 'shared/hooks/usePagination';

import { TYPES } from 'ioc/types';

import type { CargoListResponseType, ICargoListService } from './CargoList.interface';

@injectable()
export class DICargoListService implements ICargoListService {
  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.IResponseService)
  private process!: IResponseService;

  getCargoListPersonal(pageSetting: PageSetting): Promise<CargoListResponseType> {
    return this.http.post(`${GET_PERSONAL_CARGO_LIST}`, { pageSetting }, {})
      .then(this.process.getResponseData) as Promise<CargoListResponseType>;
  }

  deleteCargo(orgId: string, id: string): Promise<unknown> {
    return this.http.delete(`${DELETE_PERSONAL_CARGO_LIST}`, { urlParams: { orgId, id } })
      .then(this.process.getResponseData);
  }
}

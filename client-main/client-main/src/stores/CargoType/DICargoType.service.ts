import type { IHttpService, IResponseService } from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';

import { CARGO_TYPE_POST, CARGO_TYPE_SEARCH, MOCKED_API_PREFIX } from 'constants/constants.env';

import { TYPES } from 'ioc/types';

import type { ICargoTypeService } from './CargoType.interface';
import type { CargoCreateType } from 'types/Cargo';

@injectable()
export class DICargoTypeService implements ICargoTypeService {
  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.IResponseService)
  private process!: IResponseService;

  async searchCargoType(text: string): Promise<any> {
    return this.http.get(`${MOCKED_API_PREFIX}${CARGO_TYPE_SEARCH}?text=${text}`).then(this.process.getResponseData);
  }

  async postCargoType(data: CargoCreateType): Promise<unknown> {
    return this.http.post(`${CARGO_TYPE_POST}`, data, {}).then(this.process.getResponseData);
  }
}

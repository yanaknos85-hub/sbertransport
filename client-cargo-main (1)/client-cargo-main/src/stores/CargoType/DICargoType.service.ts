import type { IHttpService, IResponseService } from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';
import { TYPES } from 'ioc/types';

import { CARGO_TYPE_POST, CARGO_TYPE_SEARCH, MOCKED_API_PREFIX } from 'constants/constants.api';

import { CargoCreateType, ICargoTypeService } from './CargoType.interface';

@injectable()
export class DICargoTypeService implements ICargoTypeService {
  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.IResponseService)
  private process!: IResponseService;

  async searchCargoType(text: string, organizationId: string): Promise<any> {
    return this.http
      .get(
        `${MOCKED_API_PREFIX}${CARGO_TYPE_SEARCH}?text=${text}`,
        { urlParams: { organizationId } }
      )
      .then(this.process.getResponseData);
  }

  async postCargoType(data: CargoCreateType, organizationId): Promise<unknown> {
    return this.http.post(`${CARGO_TYPE_POST}`, data, { urlParams: { organizationId } }).then(this.process.getResponseData);
  }
}

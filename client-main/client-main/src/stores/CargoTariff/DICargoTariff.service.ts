import type { IHttpService, IResponseService } from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';

import { MOCKED_API_PREFIX, POST_TARIFF_TRANSPORT_TYPE, POST_TARIFF_TRANSPORT_TYPE_ALL } from 'constants/constants.env';

import { TYPES } from 'ioc/types';

import { TransportTypeEnum } from 'types/Cargo';

import { ICargoTariffService, TariffRequest } from './CargoTariff.interface';

@injectable()
export class DICargoTariffService implements ICargoTariffService {
  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.IResponseService)
  private process!: IResponseService;

  async calculateTariff(data: TariffRequest, type: TransportTypeEnum): Promise<any> {
    return this.http
      .post<TariffRequest>(`${MOCKED_API_PREFIX}${POST_TARIFF_TRANSPORT_TYPE}/${type.toUpperCase()}`, { ...data })
      .then(this.process.getResponseData);
  }

  async calculateAllTariffs(data: TariffRequest): Promise<any> {
    return this.http
      .post<TariffRequest>(`${MOCKED_API_PREFIX}${POST_TARIFF_TRANSPORT_TYPE_ALL}`, { ...data })
      .then(this.process.getResponseData);
  }
}

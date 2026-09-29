import type { IHttpService, IResponseService } from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';
import { TYPES } from 'ioc/types';

import { TransportTypeEnum } from 'types/Cargo';
import {
  MOCKED_API_PREFIX,
  POST_TARIFF_TRANSPORT_TYPE,
  POST_TARIFF_TRANSPORT_TYPE_ALL,
  POST_TARIFF_TRANSPORT_TYPE_ALL_MULTI
} from 'constants/constants.api';

import { ICargoTariffService, TariffRequest, TariffRequestMulti } from './CargoTariff.interface';

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

  async calculateAllTariffsMulti(data: TariffRequestMulti): Promise<any> {
    return this.http
      .post<TariffRequestMulti>(`${MOCKED_API_PREFIX}${POST_TARIFF_TRANSPORT_TYPE_ALL_MULTI}`, { ...data })
      .then(this.process.getResponseData)
      .catch(() => {
        return [];
      });
  }
}

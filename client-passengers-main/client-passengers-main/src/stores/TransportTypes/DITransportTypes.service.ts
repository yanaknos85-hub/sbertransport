import type { IHttpService, IResponseService } from '@sber-sbertransport/mf-core';

import { inject, injectable } from 'inversify';

import { AVAILABLE_TRANSPORTTYPES, MOCKED_API_PREFIX, TRANSPORTTYPES } from 'constants/constants.env';

import { TYPES } from 'ioc/types';

import { ITransportType, ITransportTypesService } from './TransportTypes.interface';

@injectable()
export class DITransportTypesService implements ITransportTypesService {
  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.IResponseService)
  private process!: IResponseService;

  getTransportTypes(): Promise<ITransportType[]> {
    return this.http.get<ITransportType[]>(`${MOCKED_API_PREFIX}${TRANSPORTTYPES}`).then(this.process.getResponseData);
  }

  getAvailableTransportTypes(orgId: string): Promise<ITransportType[]> {
    return this.http
      .get<ITransportType[]>(`${MOCKED_API_PREFIX}${AVAILABLE_TRANSPORTTYPES}`, { urlParams: { orgId } })
      .then(this.process.getResponseData);
  }
}

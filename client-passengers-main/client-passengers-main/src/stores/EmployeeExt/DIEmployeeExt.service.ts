import * as mfCore from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';
import { TYPES } from 'ioc/types';
import { MOCKED_API_PREFIX, EMPLOYEES_SEARCH } from 'constants/constants.env';

import { IEmpoloyeeExtService } from './EmployeeExt.interface';

@injectable()
export class DIEmpoloyeeExtService implements IEmpoloyeeExtService {
  @inject(TYPES.IConfigStore)
  private configStore!: mfCore.IConfigStore;

  @inject(TYPES.IHttpService)
  private http!: mfCore.IHttpService;

  @inject(TYPES.IResponseService)
  private process!: mfCore.IResponseService;

  private apiPrefix(): string {
    return this.configStore.isMockedApi ? MOCKED_API_PREFIX : '';
  }

  searchEmployeesByName(params: { fullName: string }): Promise<{ content: mfCore.Employee[] }> {
    return this.http
      .get<{ content: mfCore.Employee[] }>(`${this.apiPrefix()}${EMPLOYEES_SEARCH}`, {
        urlParams: { name: params.fullName },
      })
      .then(this.process.getResponseData);
  }
}

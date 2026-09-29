import type { IHttpService, IResponseService } from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';
import { TYPES } from 'ioc/types';

import {
  APPROVE_COMPENSATION, DECLINE_COMPENSATION,
  GET_COMPENSATION_NON_TERMINAL,
  GET_COMPENSATION_TERMINAL,
  MOCKED_API_PREFIX
} from 'constants/constants.api';

import { DeclineData, ICompensationService, RequestIds } from './Compensation.interface';
import { CompensationRequestModel } from './models/CargoRequest.model';
import { CompensationPaginationModel } from './models/CompensationPagination.model';
import { ICompensationRequestSearch } from './types';

@injectable()
export class DICompensationService implements ICompensationService {
  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.IResponseService)
  private process!: IResponseService;

  getCompensationListNonTerminal(
    data: ICompensationRequestSearch
  ): Promise<CompensationPaginationModel & { content: CompensationRequestModel[] }> {
    return this.http
      .post<CompensationPaginationModel & { content: CompensationRequestModel[] }>(
        `${MOCKED_API_PREFIX}/${GET_COMPENSATION_NON_TERMINAL}`,
        data
      )
      .then(this.process.getResponseData);
  }

  getCompensationListTerminal(
    data: ICompensationRequestSearch
  ): Promise<CompensationPaginationModel & { content: CompensationRequestModel[] }> {
    return this.http
      .post<CompensationPaginationModel & { content: CompensationRequestModel[] }>(
        `${MOCKED_API_PREFIX}/${GET_COMPENSATION_TERMINAL}`,
        data
      )
      .then(this.process.getResponseData);
  }

  approveRequest(requestIds: RequestIds): Promise<void> {
    return this.http
      .put<void>(
        `${MOCKED_API_PREFIX}/${APPROVE_COMPENSATION}`,
        requestIds
      )
      .then(this.process.getResponseData);
  }

  declineRequest({ requestIds, reason }: DeclineData): Promise<void> {
    return this.http
      .put<void>(
        `${MOCKED_API_PREFIX}/${DECLINE_COMPENSATION}`,
        { requestIds, reason }
      )
      .then(this.process.getResponseData);
  }
}

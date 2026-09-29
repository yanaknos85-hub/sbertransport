import type { IHttpService, IResponseService } from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';
import { TYPES } from 'ioc/types';

import {
  GET_REQUESTS_DEP,
  GET_REQUESTS_EMP,
  GET_SIBLINGS,
  LIMITREQUESTS_APPROVE_PARAMS,
  LIMITREQUESTS_CANCEL_PARAMS,
  LIMITREQUESTS_CRUD,
  LIMITREQUESTS_PARAMS
} from 'constants/constants.env';

import { DepSiblings } from './Limit.interface';
import {
  ILimitsRequestService,
  IOLimitRequestNew,
  LimitEmpRequest,
  LimitSendRequest,
  SiblingsParams,
  TLimitRequestNew
} from './LimitsRequest.interface';
import { LimitRequestModel } from './Models/LimitRequest.model';

@injectable()
export class DILimitsRequestService implements ILimitsRequestService {
  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.IResponseService)
  private process!: IResponseService;

  getLimitRequestList(): Promise<TLimitRequestNew[]> {
    return this.http
      .get<TLimitRequestNew[]>(LIMITREQUESTS_CRUD)
      .then(r => this.process.getResponseData(r, IOLimitRequestNew));
  }

  // Получение смежников подразделения
  getDepSiblings(data: SiblingsParams): Promise<DepSiblings[]> {
    return this.http
      .get<DepSiblings[]>(
        `${GET_SIBLINGS}${data.departmentId}/${data.percent}/${data.transportType}/${data.year}/${data.sum}`
      )
      .then(r => this.process.getResponseData(r));
  }

  addLimitRequest(data: LimitSendRequest): Promise<number> {
    return this.http.post(GET_REQUESTS_DEP, LimitSendRequest.encode(data)).then(this.process.getResponseStatus);
  }

  addEmpLimitRequest(data: LimitEmpRequest): Promise<number> {
    return this.http.post(GET_REQUESTS_EMP, LimitEmpRequest.encode(data)).then(this.process.getResponseStatus);
  }

  getLimitRequest(reqId: string): Promise<LimitRequestModel> {
    return this.http
      .get<LimitRequestModel>(LIMITREQUESTS_PARAMS, { urlParams: { reqId } })
      .then(r => this.process.getResponseData(r, IOLimitRequestNew));
  }

  editLimitRequest(reqId: string, data: TLimitRequestNew): Promise<number> {
    return this.http
      .put<TLimitRequestNew>(LIMITREQUESTS_PARAMS, data, { urlParams: { reqId } })
      .then(this.process.getResponseStatus);
  }

  approveLimitRequest(reqId: string): Promise<number> {
    return this.http
      .put<TLimitRequestNew>(LIMITREQUESTS_APPROVE_PARAMS, {}, { urlParams: { reqId } })
      .then(this.process.getResponseStatus);
  }

  cancelLimitRequest(reqId: string): Promise<number> {
    return this.http
      .put<TLimitRequestNew>(LIMITREQUESTS_CANCEL_PARAMS, {}, { urlParams: { reqId } })
      .then(this.process.getResponseStatus);
  }

  deleteLimitRequest(reqId: string): Promise<number> {
    return this.http
      .delete<TLimitRequestNew>(LIMITREQUESTS_PARAMS, { urlParams: { reqId } })
      .then(this.process.getResponseStatus);
  }
}

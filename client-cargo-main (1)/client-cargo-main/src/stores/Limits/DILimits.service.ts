import type { IHttpService, IResponseService } from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';
import * as t from 'io-ts';
import { TYPES } from 'ioc/types';

import {
  APPROVE_REQUEST,
  GET_ACCOUNT_BONUSES,
  GET_ALL_REQUESTS,
  GET_DEPLIMITS,
  GET_DEPLIMITS_BY_DEP,
  GET_DEPLIMITS_BY_DEP_AND_YEAR,
  GET_EMP_LIMIT,
  GET_EMPLIMITS,
  GET_LIMIT_REQUESTS,
  GET_LIMIT_SHARING,
  GET_LIMIT_TRANSFER_HISTORY,
  GET_REQUESTS_BY_APPROVER,
  GET_REQUESTS_DEP,
  GET_REQUESTS_EMP,
  GET_SPENDINGS,
  LIMIT_REQUEST_CANCEL,
  LIMITS,
  MOCKED_API_PREFIX
} from 'constants/constants.env';
import { UUID } from 'utils/io-ts';
import { ISpentActionsType } from 'modules/LimitsPage/useDetailedLimitsMapper';

import {
  Bonuses,
  ILimitsService,
  Limit,
  LimitCostHistory,
  LimitRequestInfo,
  LimitRequestSaving,
  LimitRequestSavingObject,
  LimitSharing,
  LimitTransferHistory
} from './Limit.interface';
import { LimitEmpRequest, LimitSendRequest } from './LimitsRequest.interface';

@injectable()
export class DILimitsService implements ILimitsService {
  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.IResponseService)
  private process!: IResponseService;

  getDepartmentLimits(): Promise<Limit[]> {
    return this.http.get<Limit[]>(GET_DEPLIMITS).then(this.process.getResponseData);
  }

  getDepartmentLimitsByYear(depId: UUID, year: string): Promise<Limit> {
    return this.http
      .get<Limit>(GET_DEPLIMITS_BY_DEP_AND_YEAR, { urlParams: { depId, year } })
      .then(this.process.getResponseData);
  }

  getEmployeeLimits(): Promise<Limit[]> {
    return this.http.get<Limit[]>(GET_EMPLIMITS).then(this.process.getResponseData);
  }

  getLimitSharing(limitId: string): Promise<LimitSharing[]> {
    return this.http.get<LimitSharing[]>(`${GET_LIMIT_SHARING}${limitId}`).then(this.process.getResponseData);
  }

  getEmployeeLimit(employeeId: string, year: number): Promise<Limit> {
    return this.http.get<Limit>(`${GET_EMP_LIMIT}${employeeId}/year/${year}`).then(this.process.getResponseData);
  }

  getLimitRequestsStats(): Promise<ISpentActionsType[]> {
    return this.http
      .get<ISpentActionsType[]>(`${GET_LIMIT_REQUESTS}`)
      .then(this.process.decodeResponseData(t.array(ISpentActionsType)));
  }

  cancelLimitRequest(data: { requestId: UUID; description: string }): Promise<number> {
    return this.http.put<ISpentActionsType>(LIMIT_REQUEST_CANCEL, data).then(this.process.getResponseStatus);
  }

  getLimitRequest(): Promise<LimitRequestInfo[]> {
    return this.http
      .get<LimitRequestInfo[]>(GET_REQUESTS_BY_APPROVER)
      .then(this.process.decodeResponseData(t.array(LimitRequestInfo)));
  }

  getAllLimitRequests(): Promise<LimitRequestInfo[]> {
    return this.http
      .get<LimitRequestInfo[]>(GET_ALL_REQUESTS)
      .then(this.process.decodeResponseData(t.array(LimitRequestInfo)));
  }

  // Один запрос на Approve & cancel
  approveLimitRequest(data: LimitRequestSavingObject): Promise<number> {
    return this.http
      .put<LimitRequestInfo>(APPROVE_REQUEST, LimitRequestSavingObject.encode(data), {})
      .then(this.process.getResponseStatus);
  }

  changeDepLimitRequest(data: LimitSendRequest, requestId: string): Promise<number> {
    return this.http
      .put<LimitRequestSaving>(`${GET_REQUESTS_DEP}/${requestId}`, LimitRequestSaving.encode(data), {})
      .then(this.process.getResponseStatus);
  }

  changeEmpLimitRequest(data: LimitEmpRequest, requestId: string): Promise<number> {
    return this.http
      .put<LimitRequestSaving>(`${GET_REQUESTS_EMP}/${requestId}`, LimitRequestSaving.encode(data), {})
      .then(this.process.getResponseStatus);
  }

  getLimitByDepartment(departmentId: UUID): Promise<Limit[]> {
    return this.http
      .get<Limit[]>(`${MOCKED_API_PREFIX}${GET_DEPLIMITS_BY_DEP}/${departmentId}`)
      .then(this.process.decodeResponseData(t.array(Limit)));
  }

  // Корректировки по лимиту (Получение движений ДС по лимиту)
  getLimitTransferHistory(limitId: string | UUID, year: number, maxRecords: number): Promise<LimitTransferHistory[]> {
    return this.http
      .get<LimitTransferHistory[]>(`${GET_LIMIT_TRANSFER_HISTORY}/${limitId}/${year}/${maxRecords}`)
      .then(this.process.decodeResponseData(t.array(LimitTransferHistory)));
  }

  // Расходы по лимиту
  getLimitCostHistory(
    limitId: string | UUID,
    maxRecords: number,
    organizationId: string | UUID
  ): Promise<LimitCostHistory[]> {
    return this.http
      .get<LimitCostHistory[]>(`/${LIMITS}/${GET_SPENDINGS}/${organizationId}/${limitId}/${maxRecords}`)
      .then(this.process.decodeResponseData(t.array(LimitCostHistory)));
  }

  getAccountBonuses(ownerId: string): Promise<Bonuses> {
    return this.http.get<Bonuses>(`${GET_ACCOUNT_BONUSES}${ownerId}`).then(this.process.getResponseData);
  }
}

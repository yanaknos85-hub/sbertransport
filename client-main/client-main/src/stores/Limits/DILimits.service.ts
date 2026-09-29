import type { IHttpService, IResponseService } from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';
import { stringify } from 'qs';
import * as t from 'io-ts';

import {
  APPROVE_REQUEST,
  GET_ACCOUNT_BONUSES,
  GET_ALL_REQUESTS,
  GET_DEPLIMITS,
  GET_DEPLIMITS_BY_DEP,
  GET_DEPLIMITS_BY_DEP_AND_YEAR,
  GET_EMPLIMITS,
  GET_EMP_LIMIT,
  GET_LIMITS_REQUESTS_BY_AUTHOR,
  GET_LIMIT_SHARING,
  GET_LIMIT_TRANSFER_HISTORY,
  GET_LIMITS_REQUESTS_BY_APPROVER,
  GET_ACTIVE_LIMITS_REQUESTS_BY_APPROVER,
  GET_OLD_LIMITS_REQUESTS_BY_APPROVER,
  GET_REQUESTS_DEP,
  GET_REQUESTS_EMP,
  GET_SPENDINGS,
  LIMITS,
  LIMIT_REQUEST_CANCEL,
  MOCKED_API_PREFIX,
  EMPLOYEES_SEARCH,
  GET_LIMIT_CHILDREN
} from 'constants/constants.env';

import { TYPES } from 'ioc/types';

import { ISpentActionsType } from 'modules/LimitsPage/useDetailedLimitsMapper';

import { UUID } from 'utils/io-ts';

import {
  Bonuses,
  ILimitsService,
  Limit,
  LimitCostHistory,
  LimitRequestInfo,
  ActiveLimitRequestInfo,
  OldLimitRequestInfo,
  LimitRequestSaving,
  LimitRequestSavingObject,
  LimitSharing,
  LimitTransferHistory,
  LimitResponsibleInfo,
  LimitChildren
} from './Limit.interface';
import {
  LimitChildrenRequest, LimitEmpRequest, LimitResponsiblesRequest, LimitSendRequest
} from './LimitsRequest.interface';

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

  getLimitsRequestsByAuthor(): Promise<ISpentActionsType[]> {
    return this.http
      .get<ISpentActionsType[]>(`${GET_LIMITS_REQUESTS_BY_AUTHOR}`)
      .then(this.process.decodeResponseData(t.array(ISpentActionsType)));
  }

  cancelLimitRequest(data: { requestId: UUID; description: string }): Promise<number> {
    return this.http.put<ISpentActionsType>(LIMIT_REQUEST_CANCEL, data).then(this.process.getResponseStatus);
  }

  getLimitsRequestByApprover(): Promise<LimitRequestInfo[]> {
    return this.http
      .get<LimitRequestInfo[]>(GET_LIMITS_REQUESTS_BY_APPROVER)
      .then(this.process.decodeResponseData(t.array(LimitRequestInfo)));
  }

  getActiveLimitsRequestByApprover({
    page, size, transportType,
  }: { page: number; size: number; transportType?: string }): Promise<ActiveLimitRequestInfo> {
    return this.http
      .get<ActiveLimitRequestInfo>(GET_ACTIVE_LIMITS_REQUESTS_BY_APPROVER, {
        params: {
          page, size, transportType,
        },
      })
      .then(this.process.decodeResponseData(ActiveLimitRequestInfo));
  }

  getOldLimitsRequestByApprover({
    page, size, transportType,
  }: { page: number; size: number; transportType?: string }): Promise<OldLimitRequestInfo> {
    return this.http
      .get<OldLimitRequestInfo>(GET_OLD_LIMITS_REQUESTS_BY_APPROVER, {
        params: {
          page, size, transportType,
        },
      })
      .then(this.process.decodeResponseData(OldLimitRequestInfo));
  }

  getAllLimitRequests(): Promise<LimitRequestInfo[]> {
    return this.http
      .get<LimitRequestInfo[]>(GET_ALL_REQUESTS)
      .then(this.process.decodeResponseData(t.array(LimitRequestInfo)));
  }

  getLimitRequest(requestId: UUID): Promise<LimitRequestInfo> {
    return this.http
      .get<LimitRequestInfo>(`${GET_ALL_REQUESTS}/${requestId}`)
      .then(this.process.decodeResponseData(LimitRequestInfo));
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

  getLimitChildren({ departmentId, ...params }: LimitChildrenRequest): Promise<LimitChildren> {
    return this.http
      .get<LimitChildren>(GET_LIMIT_CHILDREN, {
        urlParams: { departmentId },
        params,
      })
      .then(this.process.decodeResponseData(LimitChildren));
  }

  getLimitResponsibles(
    params: LimitResponsiblesRequest
  ): Promise<LimitResponsibleInfo> {
    return this.http
      .get<LimitResponsibleInfo>(EMPLOYEES_SEARCH, {
        params,
        paramsSerializer: params => stringify(params, { arrayFormat: 'repeat' }),
      })
      .then(this.process.decodeResponseData(LimitResponsibleInfo));
  }
}

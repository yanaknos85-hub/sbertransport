import type { IHttpService, IResponseService } from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';

import {
  DELETE_DEPARTMENT,
  DEPARTMENTS_ADD_PARAMS,
  DEPARTMENT_EDIT_PARAMS,
  GET_ALL_DEPARTMENTS,
  GET_ALL_ORGANIZATIONS,
  GET_ALL_POSITIONS,
  GET_DEPARTMENT,
  GET_ORGANIZATION,
  GET_POSITION
} from 'constants/constants.env';

import { TYPES } from 'ioc/types';

import {
  ICorporateService, IOrganization, IPosition, TDepartment
} from './Corporate.interface';
import { DepartmentDetailedModel } from './models/DepartmentDetailed.model';

@injectable()
export class DICorporateService implements ICorporateService {
  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.IResponseService)
  private process!: IResponseService;

  getAllOrganizations(): Promise<IOrganization[]> {
    return this.http
      .get<IOrganization[]>(GET_ALL_ORGANIZATIONS, {
        // На беке баг. С projection SELECT возвращается список без пагинации, но все равно только первые 20 элементов.
        // Поэтому стоит size: 200. На проде сильно нагружать это не будет, т.к. там организаций мало
        params: {
          projection: 'SELECT', page: 0, size: 200,
        },
      })
      .then(this.process.getResponseData);
  }

  getAllDepartments(orgId: string): Promise<TDepartment[]> {
    return this.http
      .get<TDepartment[]>(`${GET_ALL_DEPARTMENTS}`, {
        urlParams: { orgId },
        params: {
          projection: 'SELECT', page: 0, size: 200,
        },
      })
      .then(this.process.getResponseData);
  }

  getAllPositions(orgId: string): Promise<IPosition[]> {
    return this.http
      .get<IPosition[]>(`${GET_ALL_POSITIONS}`, { urlParams: { orgId } })
      .then(this.process.getResponseData);
  }

  getOrganization(orgId: string): Promise<IOrganization> {
    return this.http
      .get<IOrganization>(`${GET_ORGANIZATION}`, { urlParams: { orgId } })
      .then(this.process.getResponseData);
  }

  getDepartment(orgId: string, depId: string): Promise<TDepartment> {
    return this.http
      .get<TDepartment>(`${GET_DEPARTMENT}`, { urlParams: { orgId, depId } })
      .then(this.process.getResponseData);
  }

  addDepartment(orgId: string, model: DepartmentDetailedModel): Promise<TDepartment> {
    return this.http
      .post<TDepartment>(`${DEPARTMENTS_ADD_PARAMS}`, { ...model }, { urlParams: { orgId } })
      .then(this.process.getResponseData);
  }

  editDepartment(orgId: string, depId: string, model: DepartmentDetailedModel): Promise<number> {
    return this.http
      .put<number>(`${DEPARTMENT_EDIT_PARAMS}`, { ...model }, { urlParams: { orgId, depId } })
      .then(this.process.getResponseStatus);
  }

  deleteDepartment(orgId: string, depId: string): Promise<number> {
    return this.http
      .delete<number>(`${DELETE_DEPARTMENT}`, { urlParams: { orgId, depId } })
      .then(this.process.getResponseStatus);
  }

  getPosition(orgId: string, posId: string): Promise<IPosition> {
    return this.http
      .get<IPosition>(`${GET_POSITION}`, { urlParams: { orgId, posId } })
      .then(this.process.getResponseData);
  }
}

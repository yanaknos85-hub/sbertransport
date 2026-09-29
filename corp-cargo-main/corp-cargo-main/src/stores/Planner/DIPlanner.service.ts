import type { ResponseService, IHttpService } from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';
import { Employee, PaginationParams } from 'stores/Employee/Employee.interface';
import { GET_ACTIVE_DEPARTMENTS, GET_ALL_EMPLOYEES_BY_ORGANIZATION_PARAMS } from 'constants/constants.api';
import { EmployeeSearchQuery } from 'api/employee/search';
import { DepartmentType } from 'modules/Planner/types';
import { IPlannerService } from './Planner.interface';
import { TYPES } from '../../ioc/types';

@injectable()
export class DIPlannerService implements IPlannerService {
  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.IResponseService)
  private process!: ResponseService;

  getAllEmployeesByOrganization(
    orgId: string,
    query: EmployeeSearchQuery,
    pagination: PaginationParams
  ): Promise<{ content: Employee[] }> {
    return this.http
      .get<{ content: Employee[] }>(GET_ALL_EMPLOYEES_BY_ORGANIZATION_PARAMS, {
        urlParams: { orgId },
        params: { ...query, ...pagination },
      })
      .then(this.process.getResponseData);
  }

  getAllDepartmentsByOrganization(
    orgId: string,
    query: EmployeeSearchQuery,
    pagination: PaginationParams
  ): Promise<{ content: DepartmentType[] }> {
    return this.http
      .get<{ content: DepartmentType[] }>(GET_ACTIVE_DEPARTMENTS, {
        urlParams: { orgId },
        params: { ...query, ...pagination },
      })
      .then(this.process.getResponseData);
  }
}

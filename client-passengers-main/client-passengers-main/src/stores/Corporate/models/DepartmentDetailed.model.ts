/* eslint-disable @typescript-eslint/no-explicit-any */

import { EmployeeStatus, EmployeeStatusTitle } from 'modules/EmployeeApp/EmployeeApp.constants';

import { TDepartmentDetailed, TDepartmentEmployee } from '../Corporate.interface';

export class DepartmentDetailedModel implements TDepartmentDetailed {
  id?: string;

  humanReadableId?: string;

  statusTitle?: EmployeeStatusTitle;

  organization: string;

  organizationId: string;

  code: string;

  departmentName: string;

  fullStructurePath: string;

  departmentHeadId?: string;

  departmentHead?: string;

  parent?: string;

  location?: string;

  children: any[] = [];

  employees: TDepartmentEmployee[] = [];

  status: EmployeeStatus;

  constructor(department: TDepartmentDetailed) {
    this.id = department.id;
    this.humanReadableId = department.humanReadableId;
    this.organization = department.organization;
    this.organizationId = department.organizationId;
    this.code = department.code;
    this.departmentName = department.departmentName;
    this.fullStructurePath = department.fullStructurePath;
    this.departmentHeadId = department.departmentHeadId;
    this.departmentHead = department.departmentHead;
    this.parent = department.parent;
    this.location = department.location;
    this.children = department.children;
    this.employees = department.employees;
    this.status = department.status;
    this.statusTitle = EmployeeStatusTitle[department.status];
  }

  get isExisting(): boolean {
    return !!this.id;
  }

  set editStatusTitle(statusTitle: EmployeeStatusTitle) {
    this.statusTitle = statusTitle;
  }
}

import { EmployeeStatus } from 'constants/EmployeeApp.constants';

import { TDepartment, TDepartmentEmployee } from '../Corporate.interface';

export class DepartmentModel implements TDepartment {
  id?: string;

  humanReadableId?: string;

  organizationId: string;

  code: string;

  departmentName: string;

  fullStructurePath: string;

  departmentHeadId?: string;

  parentId?: string;

  location?: string;

  children: TDepartment[] = [];

  employees: TDepartmentEmployee[] = [];

  status: EmployeeStatus;

  constructor(department: TDepartment) {
    this.id = department.id;
    this.humanReadableId = department.humanReadableId;
    this.organizationId = department.organizationId;
    this.code = department.code;
    this.departmentName = department.departmentName;
    this.fullStructurePath = department.fullStructurePath;
    this.departmentHeadId = department.departmentHeadId;
    this.parentId = department.parentId;
    this.location = department.location;
    this.children = department.children;
    this.employees = department.employees;
    this.status = department.status;
  }

  get isExisting(): boolean {
    return !!this.id;
  }
}

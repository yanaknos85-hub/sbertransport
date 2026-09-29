import * as mfCore from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';
import { Dictionary, mapKeys } from 'lodash';
import { computed } from 'mobx';

import { TYPES } from 'ioc/types';

import type { ICorporateStore } from 'stores/Corporate/Corporate.interface';
import { DepartmentModel } from 'stores/Corporate/models/Department.model';
import { DepartmentDetailedModel } from 'stores/Corporate/models/DepartmentDetailed.model';

@injectable()
export class MappedStore {
  @inject(TYPES.ISelfEmployeeStore)
  private selfStore!: mfCore.ISelfEmployeeStore;

  @inject(TYPES.IEmployeeStore)
  private empStore!: mfCore.IEmployeeStore;

  @inject(TYPES.ICorporateStore)
  private corpStore!: ICorporateStore;

  @computed
  get employeeListByOrgDetailed(): Dictionary<mfCore.EmployeeDetailedModel> {
    const result = this.empStore?.employeeListByOrg.map(emp => this.mapEmployeeFields(emp)) ?? [];

    return mapKeys(result, 'id');
  }

  @computed
  get employeeListByDepMapped(): Dictionary<mfCore.EmployeeDetailedModel> {
    const result = this.empStore?.employeeListByDep.map(emp => this.mapEmployeeFields(emp)) ?? [];

    return mapKeys(result, 'id');
  }

  @computed
  get selfEmployeeDetailed(): mfCore.EmployeeDetailedModel {
    return this.mapEmployeeFields(this.selfStore.selfEmployee);
  }

  @computed
  get departmentsListDetailed(): Dictionary<DepartmentDetailedModel> {
    const result = this.corpStore.departments?.map(dep => this.mapDepartmentsFields(dep)) ?? [];

    return mapKeys(result, 'id');
  }

  mapEmployeeFields(employee: mfCore.EmployeeModel): mfCore.EmployeeDetailedModel {
    const delegatedBy
      = this.empStore?.employeeListByOrgMapped[employee.delegatedById]?.shortName || employee.delegatedById;

    const supervisor
      = this.empStore?.employeeListByOrgMapped[employee.supervisorId]?.shortName || employee.supervisorId;

    const org = this.corpStore?.organization;
    const organization = org?.officialName ?? employee.organizationId;

    const dep = this.corpStore?.department;
    const department = dep?.departmentName ?? employee.departmentId;

    const position = this.corpStore?.positionsMapped[employee.positionId]?.positionName ?? employee.positionId;

    return new mfCore.EmployeeDetailedModel({
      ...employee,
      fullName: employee.fullName,
      nameWithInitials: employee.nameWithInitials,
      delegatedBy,
      supervisor,
      organization,
      department,
      position,
    });
  }

  mapDepartmentsFields(department: DepartmentModel): DepartmentDetailedModel {
    const departmentHead = department.departmentHeadId
      ? this.empStore?.employeeListByOrgMapped[department.departmentHeadId]?.fullName || ''
      : 'Руководитель не назначен';

    const organization
      = this.corpStore.organizationsMapped[department.organizationId]?.officialName || 'Нет организации с таким uuid';
    const parent = department.parentId
      ? this.corpStore.organizationsMapped[department.parentId]?.officialName || 'Нет организации с таким uuid'
      : 'Нет родительского подразделения';

    return new DepartmentDetailedModel({
      ...department,
      departmentHead,
      organization,
      parent,
    });
  }
}

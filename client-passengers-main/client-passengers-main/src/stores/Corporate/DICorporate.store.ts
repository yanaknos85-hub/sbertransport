import * as mfCore from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';
import { Dictionary } from 'lodash';
import mapKeys from 'lodash/mapKeys';
import { action, computed, observable } from 'mobx';

import { plainToNew } from 'utils';

import { SYSTEM_MESSAGES } from 'constants/constants.app';

import { TYPES } from 'ioc/types';

import type {
  ICorporateService, ICorporateStore, IOrganization, IPosition, TDepartment
} from './Corporate.interface';
import { DepartmentModel } from './models/Department.model';
import { DepartmentDetailedModel } from './models/DepartmentDetailed.model';
import { OrganizationNormalizedModel } from './models/OrganizationNormalized.model';

@injectable()
export class DICorporateStore implements ICorporateStore {
  @inject(TYPES.ICorporateService)
  private service!: ICorporateService;

  @inject(TYPES.IResponseService)
  private process!: mfCore.IResponseService;

  @inject(TYPES.ISelfEmployeeStore)
  private selfStore!: mfCore.ISelfEmployeeStore;

  @inject(TYPES.ILogger)
  private logger!: mfCore.ILogger;

  @computed
  get selfEmployee(): mfCore.EmployeeModel {
    return this.selfStore.selfEmployee;
  }

  @observable
    organizations: IOrganization[] = [];

  @observable
    departments: DepartmentModel[] = [];

  @observable
    departmentsDetailed: DepartmentDetailedModel[] = [];

  @observable
    positions: IPosition[] = [];

  @observable
    savedDepartment: DepartmentDetailedModel | undefined = undefined;

  @computed
  get organizationsMapped(): Dictionary<OrganizationNormalizedModel> {
    const orgs = plainToNew<OrganizationNormalizedModel[]>(OrganizationNormalizedModel, this.organizations);
    return mapKeys(orgs, 'id');
  }

  @computed
  get departmentsMapped(): Dictionary<TDepartment> {
    return mapKeys(this.departments, 'id');
  }

  @computed
  get positionsMapped(): Dictionary<IPosition> {
    return mapKeys(this.positions, 'id');
  }

  @action.bound
  getDepartment(depId: string): TDepartment {
    return this.departmentsMapped[depId];
  }

  private updateDepartment(orgId: string, depId: string, department: DepartmentDetailedModel): Promise<number> {
    return this.service.editDepartment(orgId, depId, department);
  }

  private createDepartment(orgId: string, department: DepartmentDetailedModel): Promise<TDepartment> {
    return this.service.addDepartment(orgId, department);
  }

  @action.bound
  async deleteDepartment(depId: string): Promise<void> {
    const result = await this.service.deleteDepartment(this.selfEmployee.organizationId, depId);

    if (result) {
      this.process.processStatus(result, SYSTEM_MESSAGES.departmentDeleteSuccess);
      this.departments = this.departments.filter(x => x.id !== depId);
    }
  }

  @action.bound
  async editDepartment(model: DepartmentDetailedModel): Promise<void> {
    const { organizationId: orgId } = this.selfEmployee;

    if (model.id) {
      const updatedDepartment = await this.updateDepartment(orgId, model.id, model);

      if (updatedDepartment === 200) {
        this.logger.toMessage('info', SYSTEM_MESSAGES.departmentEditSuccess);
        this.savedDepartment = new DepartmentDetailedModel(model);
      }
    } else {
      const response = await this.createDepartment(orgId, model);
      const updatedDepartment = plainToNew<DepartmentDetailedModel>(DepartmentDetailedModel, response);

      if (updatedDepartment.isExisting) {
        this.savedDepartment = updatedDepartment;
        this.logger.toMessage('success', SYSTEM_MESSAGES.departmentAddSuccess);
      }
    }
  }

  @action
  async loadAllOrganizations(): Promise<void> {
    this.organizations = (await this.service.getAllOrganizations()) ?? [];
  }

  @action
  async loadAllDepartments(orgId: string): Promise<void> {
    const response = await this.service.getAllDepartments(orgId);
    this.departments = plainToNew<DepartmentModel[]>(DepartmentModel, response?.content) ?? [];
  }

  @action
  async loadAllPositions(orgId: string): Promise<void> {
    this.positions = (await this.service.getAllPositions(orgId)) ?? [];
  }

  @action.bound
  clearSavedDepartment(): void {
    this.savedDepartment = undefined;
  }

  @action.bound
  editSavedDepartment(model: DepartmentDetailedModel): void {
    this.savedDepartment = model;
  }

  @action.bound
  refreshDepartments(): void {
    this.loadAllDepartments(this.selfStore.orgId);
    this.clearSavedDepartment();
  }

  initStore(): void {
    this.loadAllOrganizations();
    this.loadAllDepartments(this.selfStore.orgId);
    this.loadAllPositions(this.selfStore.orgId);
  }
}

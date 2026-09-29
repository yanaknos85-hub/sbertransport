import * as mfCore from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';
import { action, computed, observable } from 'mobx';

import { plainToNew } from 'utils';

import { SYSTEM_MESSAGES } from 'constants/constants.app';

import { TYPES } from 'ioc/types';

import * as TransportTypesInterface from 'stores/TransportTypes/TransportTypes.interface';

import * as DelegatesInterface from './Delegates.interface';

@injectable()
export class DIDelegatesStore implements DelegatesInterface.IDelegatesStore {
  @inject(TYPES.IDelegatesService)
  private service!: DelegatesInterface.IDelegatesService;

  @inject(TYPES.ISelfEmployeeStore)
  private selfStore!: mfCore.ISelfEmployeeStore;

  @inject(TYPES.ILogger)
  private logger!: mfCore.ILogger;

  @inject(TYPES.IResponseService)
  private process!: mfCore.IResponseService;

  @inject(TYPES.ITransportTypesStore)
  private transportTypes!: TransportTypesInterface.ITransportTypesStore;

  @computed
  get selfEmployee(): mfCore.EmployeeModel {
    return this.selfStore.selfEmployee;
  }

  @observable
    delegates: DelegatesInterface.DelegateModel[] = [];

  @observable
    candidatesToDelegates: Record<string, mfCore.EmployeeModel[]> = {};

  @observable
    selfCandidatesToDelegates: mfCore.EmployeeModel[] = [];

  @computed
  get namesWithInitials(): Record<string, string> {
    const getNameWithInitials = (employee: mfCore.Employee): string => `${employee.firstName.charAt(0)}. ${employee.patronymic?.charAt(0)}. ${employee.lastName}`;

    return this.delegates.reduce((names: Record<string, string>, delegate) => {
      names[delegate.delegateId] = getNameWithInitials(delegate.delegateEmployee);
      // FIXME no-param-reassign
      return names;
    }, {});
  }

  @action.bound
  async getDelegates(): Promise<void> {
    const data = await this.service.getDelegates({
      orgId: this.selfEmployee.organizationId,
      depId: this.selfEmployee.departmentId,
      supId: this.selfEmployee.id,
    });
    this.delegates = plainToNew<DelegatesInterface.DelegateModel[]>(DelegatesInterface.DelegateModel, data) ?? [];
  }

  @action.bound
  async getCandidatesToDelegates(transType: string, date: string): Promise<void> {
    const data = await this.service.getCandidatesToDelegates({
      orgId: this.selfEmployee.organizationId,
      depId: this.selfEmployee.departmentId,
      supId: this.selfEmployee.id,
      transType,
      date,
    });

    this.candidatesToDelegates = Object.assign(this.candidatesToDelegates, {
      [transType]: plainToNew<mfCore.EmployeeModel[]>(mfCore.EmployeeModel, data) ?? [],
    });
  }

  @action.bound
  async getSelfCandidatesToDelegates(transportType: TransportTypesInterface.TransportTypeEnum, date: string): Promise<void> {
    const data = await this.service.getSelfCandidatesToDelegates(transportType, date);

    this.selfCandidatesToDelegates = plainToNew<mfCore.EmployeeModel[]>(mfCore.EmployeeModel, data) ?? [];
  }

  @action.bound
  async addDelegate(delegate: DelegatesInterface.DelegateModel): Promise<void> {
    const data = await this.service.addDelegate(
      { orgId: this.selfEmployee.organizationId, depId: this.selfEmployee.departmentId },
      { ...delegate }
    );

    const result = plainToNew<DelegatesInterface.DelegateModel>(DelegatesInterface.DelegateModel, data);

    if (result) {
      this.delegates = [...this.delegates, result];
      this.logger.toMessage('success', SYSTEM_MESSAGES.delegateSuccessfull);
    }
  }

  @action.bound
  async deleteDelegate(delegateId: string): Promise<void> {
    const result = await this.service.deleteDelegate({
      orgId: this.selfEmployee.organizationId,
      depId: this.selfEmployee.departmentId,
      delegateId,
    });

    if (result) {
      this.process.processStatus(result, SYSTEM_MESSAGES.delegateDeleteSuccess);
      this.delegates = this.delegates.filter(x => x.id !== delegateId);
    }
  }

  initStore(): void {
    this.getDelegates();
  }

  @action.bound
  async searchSelfDelegateCandidates(
    { transportType, ...params }: mfCore.RequestParams,
    cancelerSetter?: mfCore.RequestCancelerSetter
  ): Promise<mfCore.EmployeeModel[]> {
    const data = await this.service.searchSelfDelegateCandidates(transportType, params, cancelerSetter);
    return plainToNew<mfCore.EmployeeModel[]>(mfCore.EmployeeModel, data) ?? [];
  }
}

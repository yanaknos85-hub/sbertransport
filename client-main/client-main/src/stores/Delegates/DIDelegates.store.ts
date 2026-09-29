import * as mfCore from '@sber-sbertransport/mf-core';
import { AxiosError } from 'axios';

import { inject, injectable } from 'inversify';
import { action, computed, observable } from 'mobx';

import { SYSTEM_MESSAGES } from 'constants/constants.app';

import { TYPES } from 'ioc/types';

import * as TransportTypesInterface from 'stores/TransportTypes/TransportTypes.interface';
import { PaginationParams } from 'stores/Pagination/Pagination.interface';

import { plainToNew } from 'utils';
import { formatName } from 'utils/formatName';
import { getDelegateErrorMessage } from 'modules/ApprovalPage/utils';

import * as DelegatesInterface from './Delegates.interface';
import { DelegateModel } from './Delegates.interface';

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
    delegateResponseInfo: Partial<DelegatesInterface.DelegateResponseInfo> = {};

  @observable
    isLoadingDelegates = false;

  @observable
    candidatesToDelegates: Record<string, mfCore.EmployeeModel[]> = {};

  @observable
    selfCandidatesToDelegates: mfCore.EmployeeModel[] = [];

  @computed
  get fullDelegateNames(): Record<string, string> {
    return this.delegates.reduce((names: Record<string, string>, delegate) => {
      names[delegate.delegateId] = formatName(delegate.delegateEmployee);
      return names;
    }, {});
  }

  @action.bound
  async getDelegates(query: PaginationParams): Promise<void> {
    try {
      this.isLoadingDelegates = true;
      const data = await this.service.getDelegates({
        orgId: this.selfEmployee.organizationId,
        depId: this.selfEmployee.departmentId,
        supId: this.selfEmployee.id,
        ...query,
      });
      this.delegateResponseInfo = data;
      this.delegates = plainToNew<DelegatesInterface.DelegateModel[]>(DelegatesInterface.DelegateModel, data.content) ?? [];
    } finally {
      this.isLoadingDelegates = false;
    }
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
  addDelegate(delegate: DelegatesInterface.DelegateModel): Promise<void> {
    return this.service.addDelegate(
      { orgId: this.selfEmployee.organizationId, depId: this.selfEmployee.departmentId },
      { ...delegate }
    ).then(data => {
      const result = plainToNew<DelegatesInterface.DelegateModel>(DelegatesInterface.DelegateModel, data);

      if (result) {
        this.logger.toMessage('success', SYSTEM_MESSAGES.delegateSuccessfull);
      }
    }).catch((error: AxiosError) => {
      this.logger.toMessage('error', getDelegateErrorMessage(error));
      throw new Error();
    });
  }

  @action.bound
  updateDelegate(delegate: DelegatesInterface.DelegateModel): Promise<void> {
    return this.service.updateDelegate(
      { orgId: this.selfEmployee.organizationId, depId: this.selfEmployee.departmentId },
      { ...delegate }
    ).then(() => {
      this.delegates = this.delegates.map((item: DelegateModel) => ({
        ...item,
        ...(item.id === delegate.id && delegate),
      }));
      this.process.processStatus(200, SYSTEM_MESSAGES.delegateUpdateSuccess);
    }).catch((error: AxiosError) => {
      this.logger.toMessage('error', getDelegateErrorMessage(error));
      throw new Error();
    });
  }

  @action.bound
  deleteDelegate(delegateId: string): Promise<void> {
    return this.service.deleteDelegate({
      orgId: this.selfEmployee.organizationId,
      depId: this.selfEmployee.departmentId,
      delegateId,
    })
      .then(() => {
        this.process.processStatus(200, SYSTEM_MESSAGES.delegateDeleteSuccess);
      })
      .catch(() => {
        this.logger.toMessage('error', SYSTEM_MESSAGES.delegateDeleteError);
        throw new Error();
      });
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

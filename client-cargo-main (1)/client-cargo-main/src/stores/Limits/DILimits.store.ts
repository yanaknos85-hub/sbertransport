import * as mfCore from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';
import { TYPES } from 'ioc/types';
import { mapKeys } from 'lodash';
import { action, computed, observable } from 'mobx';
import { plainToNew } from 'utils';

import { SYSTEM_MESSAGES } from 'constants/constants.app';
import type { UUID } from 'utils/io-ts';
import { ISpentActionsType } from 'modules/LimitsPage/useDetailedLimitsMapper';

import * as LimitInterface from './Limit.interface';
import { LimitEmpRequest, LimitSendRequest } from './LimitsRequest.interface';
import { LimitModel } from './Models/LimitModel';

@injectable()
export class DILimitsStore implements LimitInterface.ILimitsStore {
  @inject(TYPES.ILimitsServiceNew)
  private service!: LimitInterface.ILimitsService;

  @inject(TYPES.ISelfEmployeeStore)
  private selfStore!: mfCore.ISelfEmployeeStore;

  @inject(TYPES.IResponseService)
  private process!: mfCore.IResponseService;

  @computed
  get selfEmployee(): mfCore.EmployeeModel {
    return this.selfStore.selfEmployee;
  }

  @observable
    limitsIsLoaded = false;

  @observable
    currentDepartmentSharing: LimitInterface.LimitSharing[] = [];

  @observable
    currentEmployeeSharing: LimitInterface.LimitSharing[] = [];

  @observable
    currentLimit: LimitModel[] | undefined;

  @observable
    employeeLimit: LimitInterface.Limit | undefined;

  @observable
    limitRequestsStats: ISpentActionsType[] = [];

  @observable
    departmentLimits: LimitModel[] = [];

  @observable
    employeeLimits: LimitModel[] = [];

  @observable
    limitSharing: LimitInterface.LimitSharing[] = [];

  @observable
    limitRequests: LimitInterface.LimitRequestInfo[] = [];

  @observable
    allLimitRequests: LimitInterface.LimitRequestInfo[] = [];

  @observable
    depLimits: LimitInterface.Limit[] = [];

  @observable
    currentRequest: ISpentActionsType | undefined = undefined;

  @observable
    limitTransferHistory: LimitInterface.LimitTransferHistory[] = [];

  @observable
    limitCostHistory: LimitInterface.LimitCostHistory[] = [];

  @observable
    bonuses: LimitInterface.Bonuses | undefined;

  @computed
  get listMapped(): Dictionary<ISpentActionsType> {
    return mapKeys(this.limitRequestsStats, 'requestId');
  }

  @action
  setCurrentRequest(id: string): void {
    this.currentRequest = this.listMapped[id];
  }

  @action
  async cancelLimitRequest(data: { requestId: UUID; description: string }): Promise<void> {
    const result = await this.service.cancelLimitRequest(data);
    const isCanceled = this.process.processStatus(result, SYSTEM_MESSAGES.limitRequestCancelSuccess);
    if (isCanceled) {
      await this.getLimitRequestsStats();
    }
  }

  private getDepartmentLimitByYear = (date: Date = new Date()): LimitModel[] | undefined => {
    this.currentLimit = this.departmentLimits.filter(
      limit => limit.department?.id === this.selfEmployee.departmentId && limit.year === date.getFullYear()
    );
    return this.currentLimit;
  };

  private getCurrentLimitYear = (): number => {
    if (this.currentLimit) {
      return this.currentLimit[0]?.year;
    }
    return new Date().getFullYear();
  };

  public getLimitsIdByDepartmentId = (depId: string): string | undefined => {
    const currentYear = new Date().getFullYear();
    const foundDeps = this.departmentLimits.find(
      limit => limit.department?.id === depId && limit.year === currentYear
    );
    return foundDeps?.id;
  };

  @action
  async getDepartmentLimitsByYear(departmentId: UUID, year: string): Promise<LimitModel[]> {
    const data = await this.service.getDepartmentLimitsByYear(departmentId, year);
    this.departmentLimits = [plainToNew(LimitModel, data)] ?? [];
    return this.departmentLimits;
  }

  @action
  async getDepartmentLimits(): Promise<LimitModel[]> {
    const data = await this.service.getDepartmentLimits();
    this.departmentLimits = plainToNew(LimitModel, data) ?? [];
    return this.departmentLimits;
  }

  @action
  async getEmployeeLimits(): Promise<void> {
    const data = await this.service.getEmployeeLimits();
    this.employeeLimits = plainToNew(LimitModel, data) ?? [];
  }

  @action
  async getLimitSharing(limitId: UUID): Promise<LimitInterface.LimitSharing[]> {
    let data: LimitInterface.LimitSharing[] = [];
    if (limitId) {
      data = await this.service.getLimitSharing(limitId);
    }
    this.limitSharing = data;
    return data;
  }

  @action
  setCurrentEmployeeSharing(limits: LimitInterface.LimitSharing[]): LimitInterface.LimitSharing[] {
    this.currentEmployeeSharing = limits;
    return limits;
  }

  @action
  async setCurrentDepartmentSharing(): Promise<LimitInterface.LimitSharing[]> {
    const limits = this.currentLimit;

    if (limits) {
      await Promise.all(limits.map(limit => this.getLimitSharing(limit.id))).then(responses => {
        this.currentDepartmentSharing = [];
        responses.forEach(response => {
          this.currentDepartmentSharing = [...this.currentDepartmentSharing, ...response];
        });
      });
    }
    return this.currentDepartmentSharing || [];
  }

  @action
  async getEmployeeLimit(): Promise<LimitInterface.Limit> {
    const data = await this.service.getEmployeeLimit(this.selfEmployee?.id as UUID, this.getCurrentLimitYear());

    this.employeeLimit = data;
    return data;
  }

  @action
  async getLimitRequestsStats(): Promise<ISpentActionsType[]> {
    const data = await this.service.getLimitRequestsStats();
    this.limitRequestsStats = data;
    return data;
  }

  async getLimitRequest(): Promise<LimitInterface.LimitRequestInfo[]> {
    const data = await this.service.getLimitRequest();
    this.limitRequests = data;
    return data;
  }

  @action
  async getAllLimitRequests(): Promise<LimitInterface.LimitRequestInfo[]> {
    const data = await this.service.getAllLimitRequests();
    this.allLimitRequests = data;
    return data;
  }

  @action
  async getLimitByDepartment(departmentId: UUID): Promise<LimitInterface.Limit[]> {
    const data = await this.service.getLimitByDepartment(departmentId);
    this.depLimits = data;
    this.departmentLimits = data as any;
    return data;
  }

  @action
  async getLimitTransferHistory(
    limitId: string | UUID,
    year: number,
    maxRecords: number
  ): Promise<LimitInterface.LimitTransferHistory[]> {
    const data = await this.service.getLimitTransferHistory(limitId, year, maxRecords);
    this.limitTransferHistory = data;
    return data;
  }

  @action
  async getLimitCostHistory(
    limitId: string | UUID,
    maxRecords: number,
    orgId: string | UUID
  ): Promise<LimitInterface.LimitCostHistory[]> {
    const data = await this.service.getLimitCostHistory(limitId, maxRecords, orgId);
    this.limitCostHistory = data;
    return data;
  }

  @action
  async approveLimitRequest(data: LimitInterface.LimitRequestSavingObject): Promise<number> {
    let res = 0;
    // eslint-disable-next-line no-return-assign
    const result = await this.service.approveLimitRequest(data).then(status => (res = status));
    const isApproved = data.approvalState === 'APPROVED';
    const isCanceled = isApproved
      ? this.process.processStatus(result, SYSTEM_MESSAGES.limitIsApproved)
      : this.process.processStatus(result, SYSTEM_MESSAGES.limitRequestCancelSuccess);
    if (isCanceled) {
      await this.getLimitRequest();
      await this.getAllLimitRequests();
    }
    return res;
  }

  @action
  async changeDepLimitRequest(data: LimitSendRequest, requestId: string): Promise<number> {
    return this.service.changeDepLimitRequest(data, requestId);
  }

  @action
  async changeEmpLimitRequest(data: LimitEmpRequest, requestId: string): Promise<number> {
    return this.service.changeEmpLimitRequest(data, requestId);
  }

  @action
  async getAccountBonuses(): Promise<LimitInterface.Bonuses> {
    const data = await this.service.getAccountBonuses(this.selfEmployee.id);
    this.bonuses = data;
    return data;
  }

  @action.bound
  refreshLimits(): void {
    this.initStore();
  }

  @action.bound
  getLimits(): void {
    this.getLimitByDepartment(this.selfStore.depId as UUID)
    // .then(response => {
    //   console.log('лимиты подразделений', toJS(response))
    // })

      // Отфильтровываем лимиты подзраделений по departmentId пользователя и году
      // устанавливаем this.currentLimit (далее ПОЛЬЗОВАТЕЛЬСКИЕ ЛИМИТЫ ПОДРАЗДЕЛЕНИЙ)
      .then(() => this.getDepartmentLimitByYear())
      // .then(response => {
      //   console.log(`departmentId пользователя ${this.selfEmployee.departmentId}`);
      //   console.log('ПОЛЬЗОВАТЕЛЬСКИЕ ЛИМИТЫ ПОДРАЗДЕЛЕНИЙ', toJS(response));
      //   return response;
      // })

      // Если лимитов у пользователя нет, то и дальше нем смысла что-то запрашивать
      .then(response => {
        if (!response || !response.length) {
          throw new Error('пользовательские лимиты не найдены');
        }
        return response;
      })

      // Получаем лимит пользователя /limits/emplimits/getByEmployeeAndYear/${employeeId}/year/${year}
      .then(() => this.getEmployeeLimit()) // Тут приходит ничего от сервера!
      // .then(response => {
      //   console.log('лимит пользователя', toJS(response ||  ': - не найден / нет ответа от сервера -'))
      //   return response;
      // })

      // Получаем шэринг лимитов на основе лимита пользователя если у него есть личный лимит /limits/limitsharing/getByLimit/full/${limitId}
      .then(employeeLimit => this.getLimitSharing(employeeLimit.id))
      // .then(response => {
      //   console.log('шэринг лимитов', toJS(response))
      //   return response;
      // })

      // устанавливаем ЛИЧНЫЙ ЛИМИТ (this.currentEmployeeSharing)
      // вывыодим ЛИЧНЫЙ ЛИМИТ если он есть или показываем кнопку "Запросить лимит" (на странице LimitPage в прогресс баре)
      .then(response => this.setCurrentEmployeeSharing(response))
      // .then(response => {
      //   console.log('ЛИЧНЫЙ ЛИМИТ', toJS(response));
      //   return response;
      // })

      // Делаем кучу запросов на основе ПОЛЬЗОВАТЕЛЬСКИХ ЛИМИТОВ ПОДРАЗДЕЛЕНИЙ: currentLimit.map(limitId -> /limits/limitsharing/getByLimit/full/${limitId})
      // Конкатинируем
      // устанавливаем ЛИМИТ ПОДРАЗДЕЛЕНИЯ (this.currentDepartmentSharing)
      // вывыодим ЛИМИТ ПОДРАЗДЕЛЕНИЯ если он есть или показываем кнопку "Запросить лимит" (на странице LimitPage в прогресс баре)
      .then(() => this.setCurrentDepartmentSharing()) // устанавливаем ЛИМИТ ПОДРАЗДЕЛЕНИЯ
      // .then(response => {
      //   console.log('ЛИМИТ ПОДРАЗДЕЛЕНИЯ', toJS(response));
      //   return response;
      // })

      // eslint-disable-next-line no-console
      .catch(err => console.log(err))

      .finally(() => {
        this.limitsIsLoaded = true;
      });
  }

  @action.bound
  initStore(): void {
    this.getLimits();
  }
}

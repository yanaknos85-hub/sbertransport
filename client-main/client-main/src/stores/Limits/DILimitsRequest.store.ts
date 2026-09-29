import type { IResponseService } from '@sber-sbertransport/mf-core';

import { inject, injectable } from 'inversify';
import { Dictionary, mapKeys } from 'lodash';
import { action, computed, observable } from 'mobx';

import { SYSTEM_MESSAGES } from 'constants/constants.app';

import { TYPES } from 'ioc/types';

import { plainToNew } from 'utils';

import { DepSiblings } from './Limit.interface';
import * as LimitsRequestInterface from './LimitsRequest.interface';
import { LimitRequestModel } from './Models/LimitRequest.model';

@injectable()
export class DILimitsRequestStore implements LimitsRequestInterface.ILimitsRequestStore {
  @inject(TYPES.ILimitsRequestService)
  private service!: LimitsRequestInterface.ILimitsRequestService;

  @inject(TYPES.IResponseService)
  private process!: IResponseService;

  @observable
    list: LimitRequestModel[] = [];

  @observable
    siblings: DepSiblings[] = [];

  @computed
  get listMapped(): Dictionary<LimitRequestModel> {
    return mapKeys(this.list, 'id');
  }

  @observable
    currentRequest: LimitRequestModel | undefined = undefined;

  @action
    getList = async (): Promise<void> => {
      const result = await this.service.getLimitRequestList();
      this.list = plainToNew(LimitRequestModel, result) ?? [];
    };

  @action
    getDepSiblings = async (data: LimitsRequestInterface.SiblingsParams): Promise<DepSiblings[]> => {
      const result = await this.service.getDepSiblings(LimitsRequestInterface.SiblingsParams.encode(data));
      this.siblings = result;
      return result;
    };

  @action
  createRequest(data: LimitsRequestInterface.LimitSendRequest): void {
    this.service.addLimitRequest(data);
  }

  @action
  // eslint-disable-next-line no-return-await
    addLimitRequest = async (data: LimitsRequestInterface.LimitSendRequest): Promise<number> => await this.service.addLimitRequest(data);

  @action
  // eslint-disable-next-line no-return-await
    addEmpLimitRequest = async (data: LimitsRequestInterface.LimitEmpRequest): Promise<number> => await this.service.addEmpLimitRequest(data);

  @action
  async editLimitRequest(reqId: string, data: LimitsRequestInterface.TLimitRequestNew): Promise<void> {
    this.currentRequest = undefined;

    const result = await this.service.editLimitRequest(reqId, data);
    const isEdited = this.process.processStatus(result, SYSTEM_MESSAGES.limitRequestEditSuccess);

    if (isEdited) {
      this.currentRequest = await this.getRequest(reqId);
      this.getList();
    }
  }

  @action
    getRequest = async (reqId: string): Promise<LimitRequestModel> => {
      const result = await this.service.getLimitRequest(reqId);
      return new LimitRequestModel(result);
    };

  @action
  setCurrentRequest(id: string): void {
    this.currentRequest = this.listMapped[id];
  }

  @action
  async cancelRequest(id: string, reason: string): Promise<void> {
    const result = await this.service.cancelLimitRequest(id, reason);
    const isCanceled = this.process.processStatus(result, SYSTEM_MESSAGES.limitRequestCancelSuccess);
    if (isCanceled) {
      this.getList();
    }
  }

  initStore(): void {
    this.getList();
  }
}

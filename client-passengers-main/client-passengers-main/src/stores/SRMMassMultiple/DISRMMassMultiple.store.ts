import type { ILogger } from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';
import { action, observable } from 'mobx';

import { TYPES } from 'ioc/types';

import * as SRMMassMultipleInterface from './SRMMassMultiple.interface';

@injectable()
export class DISRMMassStoreMultiple implements SRMMassMultipleInterface.ISRMMassStoreMultiple {
  @inject(TYPES.ISRMMassServiceMultiple)
  private service!: SRMMassMultipleInterface.ISRMMassServiceMultiple;

  @inject(TYPES.ILogger)
  private logger!: ILogger;

  @observable
    uploadRequestId = 'a75b8d90-c1b2-4388-913c-2401c8c38bb3';

  @observable
    request: SRMMassMultipleInterface.Request | undefined;

  @observable
    requestList: SRMMassMultipleInterface.RequestListItem[] = [];

  @observable
    validatedFile: File | null = null;

  @action
  async saveFile(userId: any): Promise<void> {
    const errorMessage = 'Ошибка загрузки файла';

    try {
      // @ts-ignore
      const response = await this.service.saveFile(this.validatedFile, userId);
      if (response) {
        const { requestId } = response;
        this.uploadRequestId = requestId;
        this.requestList = [];
        this.validatedFile = null;
      } else {
        this.validatedFile = null;
        this.requestList = [];
        throw new Error(errorMessage);
      }
    } catch (err: any) {
      const error = err.message || errorMessage;
      throw new Error(error);
    }
  }

  @action
  async validateFile(file: File, userId: any): Promise<void> {
    const errorMessage = 'Ошибка загрузки файла';

    try {
      const response = await this.service.validateFile(file, userId);
      if (response) {
        const { requestId } = response;
        this.uploadRequestId = requestId;
        this.validatedFile = file;
        // @ts-ignore
        this.requestList = response?.content;
      } else {
        this.validatedFile = null;
        this.requestList = [];
        throw new Error(errorMessage);
      }
    } catch (err: any) {
      const error = err.message || errorMessage;
      throw new Error(error);
    }
  }
}

import type { ILogger, ISelfEmployeeStore } from '@sber-sbertransport/mf-core';

import { UploadFile } from 'antd/lib/upload/interface';
import { inject, injectable } from 'inversify';
import { action, observable } from 'mobx';

import { SYSTEM_MESSAGES } from 'constants/constants.app';

import { TYPES } from 'ioc/types';

import type {
  IFilesService,
  IFilesStore,
  TUploadHandbookParams,
  TUploadImportSummary,
  TUploadParsingSummary
} from './Files.interface';

@injectable()
export class DIFilesStore implements IFilesStore {
  @inject(TYPES.ISelfEmployeeStore)
  private self!: ISelfEmployeeStore;

  @inject(TYPES.IFilesService)
  private service!: IFilesService;

  @inject(TYPES.ILogger)
  private logger!: ILogger;

  @observable
    parsingSummary: TUploadParsingSummary | undefined = undefined;

  @observable
    loadingSummary: TUploadImportSummary | undefined = undefined;

  @observable
    isUploading = false;

  @action.bound
  clearSummary(): void {
    this.parsingSummary = undefined;
    this.loadingSummary = undefined;
  }

  private getDataAppended = (values: { upload: UploadFile[] }): FormData => {
    const file = values?.upload[0]?.originFileObj as Blob;
    const data = new FormData();
    data.append('file', file);
    return data;
  };

  preloadHandbook = async (values: TUploadHandbookParams): Promise<void> => {
    const data = this.getDataAppended(values);
    this.isUploading = true;
    this.service.preloadFile({
      ...values, orgId: this.self.orgId, data,
    }).then(
      result => {
        this.isUploading = false;
        this.parsingSummary = result;

        if (result.parsingResult.parseStatus === 'OK') {
          this.logger.toMessage('success', SYSTEM_MESSAGES.simulationSuccess);
        }
      },
      (error: any) => {
        this.isUploading = false;
        return error;
      }
    );
  };

  loadHandbook = async (values: TUploadHandbookParams): Promise<void> => {
    const data = this.getDataAppended(values);
    this.isUploading = true;
    this.service.uploadFile({
      ...values, orgId: this.self.orgId, data,
    }).then(
      result => {
        this.isUploading = false;
        this.loadingSummary = result;

        // if (result.parsingResult.parseStatus === 'OK') {
        this.logger.toMessage('success', SYSTEM_MESSAGES.fileUploadSuccess);
        // }
      },
      (error: any) => {
        this.isUploading = false;
        return error;
      }
    );
  };

  // eslint-disable-next-line @typescript-eslint/no-empty-function
  initStore = (): void => {};
}

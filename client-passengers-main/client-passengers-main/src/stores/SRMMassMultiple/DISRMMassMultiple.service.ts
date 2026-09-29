import type { IHttpService, IResponseService } from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';
import {
  FILE_LOADER, FILE_VALIDATE
} from 'constants/constants.env';

import { TYPES } from 'ioc/types';

import { ISRMMassServiceMultiple, SavedFileInfo } from './SRMMassMultiple.interface';

@injectable()
export class DISRMMassServiceMultiple implements ISRMMassServiceMultiple {
  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.IResponseService)
  private process!: IResponseService;

  async saveFile(file: File /* , userId: UUID */): Promise<SavedFileInfo | void> {
    const formData = new FormData();
    formData.append('file', file);
    try {
      return await this.http
        .post<SavedFileInfo>(`${FILE_LOADER}file`, formData)
        .then(x => this.process.getResponseData(x, SavedFileInfo));
    } catch (err: any) {
      throw new Error(err?.response?.data?.message);
    }
  }

  async validateFile(file: File): Promise<SavedFileInfo | void> {
    const formData = new FormData();
    formData.append('file', file);
    try {
      return await this.http
        .post<SavedFileInfo>(FILE_VALIDATE, formData)
        .then(x => this.process.getResponseData(x, SavedFileInfo));
    } catch (err: any) {
      throw new Error(err?.response?.data?.message);
    }
  }
}

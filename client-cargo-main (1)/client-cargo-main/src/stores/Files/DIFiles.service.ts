import type { IHttpService, IResponseService } from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';
import { TYPES } from 'ioc/types';

import { PRELOADFILE, UPLOADFILE } from 'constants/constants.env';

import {
  getFilesArgs,
  IFilesService,
  IOUploadImportSummary,
  IOUploadParsingSummary,
  TUploadImportSummary,
  TUploadParsingSummary
} from './Files.interface';

@injectable()
export class DIFilesService implements IFilesService {
  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.IResponseService)
  private process!: IResponseService;

  uploadFile(args: getFilesArgs): Promise<TUploadImportSummary> {
    const {
      orgId, decSeparator, nsi, separator, strategyMode, data,
    } = args;

    return this.http
      .postFormData<TUploadImportSummary>(UPLOADFILE, data, {
        urlParams: {
          orgId,
          decSeparator,
          nsi,
          separator,
          strategyMode,
        },
      })
      .then(result => this.process.getResponseData(result, IOUploadImportSummary));
  }

  preloadFile(args: getFilesArgs): Promise<TUploadParsingSummary> {
    const {
      orgId, decSeparator, nsi, separator, strategyMode, data,
    } = args;

    return this.http
      .postFormData<TUploadParsingSummary>(PRELOADFILE, data, {
        urlParams: {
          orgId,
          decSeparator,
          nsi,
          separator,
          strategyMode,
        },
      })
      .then(result => this.process.getResponseData(result, IOUploadParsingSummary));
  }
}

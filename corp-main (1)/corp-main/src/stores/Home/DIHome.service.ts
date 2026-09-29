/* eslint-disable @typescript-eslint/no-explicit-any */
import type { IHttpService, IResponseService } from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';

import { TYPES } from 'ioc/types';
import {
  GET_METRICS, GET_UI_PREFERENCES, SAVE_NOTIFICATION, SET_UI_PREFERENCES
} from 'constants/constants.api';
import { IHomeService, INotification, IUiPreferences } from './Home.interface';
import { IMetricsMain } from 'modules/RedesignHome/types/Home.types';

@injectable()
export class DIHomeService implements IHomeService {
  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.IResponseService)
  private process!: IResponseService;

  getUiPreferences(userID, nameForm): Promise<IUiPreferences> {
    return this.http
      .get<IUiPreferences>(`${GET_UI_PREFERENCES}`, { params: { userID, nameForm } })
      .then(this.process.getResponseData);
  }

  setUiPreferences(userID, data): Promise<IUiPreferences> {
    return this.http
      .put<IUiPreferences>(`${SET_UI_PREFERENCES}`, data)
      .then(this.process.getResponseData);
  }

  getMetrics(data): Promise<IMetricsMain> {
    return this.http
      .post<IMetricsMain>(`${GET_METRICS}`, data, { timeout: 120_000 })
      .then(this.process.getResponseData);
  }

  saveNotification(data): Promise<INotification> {
    return this.http
      .post<INotification>(`${SAVE_NOTIFICATION}`, data)
      .then(this.process.getResponseData);
  }
}

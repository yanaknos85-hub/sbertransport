/* eslint-disable @typescript-eslint/no-explicit-any */
import type { IHttpService, IResponseService } from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';

import { TYPES } from 'ioc/types';
import {
  GET_UI_PREFERENCES, SET_UI_PREFERENCES, SET_UI_PREFERENCES_OTO, GET_UI_PREFERENCES_OTO
} from 'constants/constants.api';
import { IRegistryService, IUiPreferences } from './Registry.interface';

@injectable()
export class DIRegistryService implements IRegistryService {
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

  getUiPreferencesOto(userID, nameForm): Promise<IUiPreferences> {
    return this.http
      .get<IUiPreferences>(`${GET_UI_PREFERENCES_OTO}`, { params: { userID, nameForm } })
      .then(this.process.getResponseData);
  }

  setUiPreferencesOto(userID, data): Promise<IUiPreferences> {
    return this.http
      .put<IUiPreferences>(`${SET_UI_PREFERENCES_OTO}`, data)
      .then(this.process.getResponseData);
  }
}

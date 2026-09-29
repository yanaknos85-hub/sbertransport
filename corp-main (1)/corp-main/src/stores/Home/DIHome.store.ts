/* eslint-disable no-underscore-dangle */
import { inject, injectable } from 'inversify';
import { action, observable } from 'mobx';

import type {
  IHomeService, INotification, IUiPreferences
} from './Home.interface';
import { TYPES } from 'ioc/ioc.types';
import type { IHotButtons, IMetricsMain, IMetricsMainData } from 'modules/RedesignHome/types/Home.types';

@injectable()
export class DIHomeStore {
  @inject(TYPES.IHomeService)
  private service!: IHomeService;

  @observable
    saveSettings: IHotButtons[] = [];

  @observable
    metricsData: IMetricsMain = {};

  @action.bound
  setSaveSettings(data: IHotButtons[]) {
    this.saveSettings = data;
  }

  @action
  async setUiPreferences(userID: string | undefined, data: IUiPreferences): Promise<IUiPreferences> {
    return await this.service.setUiPreferences(userID, data);
  }

  @action
  setMetricsData(data: IMetricsMain) {
    this.metricsData = data;
  }

  @action
  async getUiPreferences(userID: string | undefined, name: string): Promise<IUiPreferences> {
    return await this.service.getUiPreferences(userID, name);
  }

  @action
  async getMetrics(data: IMetricsMainData): Promise<IMetricsMain> {
    return await this.service.getMetrics(data);
  }

  @action
  async saveNotification(data: INotification): Promise<INotification> {
    return await this.service.saveNotification(data);
  }
}

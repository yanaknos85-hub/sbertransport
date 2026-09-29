/* eslint-disable no-underscore-dangle */
import { inject, injectable } from 'inversify';
import { action, observable } from 'mobx';

import type {
  IRegistryService, IRegistryStore, IUiPreferences, SelectedRowData
} from './Registry.interface';
import { TYPES } from 'ioc/ioc.types';

@injectable()
export class DIRegistryStore implements IRegistryStore {
  @inject(TYPES.IRegistryService)
  private service!: IRegistryService;

  @observable
    selectedRowData: SelectedRowData[] = [];

  @observable
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    saveSettings: any = [];

  @action.bound
  setSelectedRowData(data: SelectedRowData[]) {
    data.forEach(newItem => {
      const index = this.selectedRowData.findIndex(el => el.requestId === newItem.requestId);

      if (index === -1) {
        this.selectedRowData.push(newItem);
      } else {
        this.selectedRowData[index] = newItem;
      }
    });
  }

  @action.bound
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  setSaveSettings(data: any[]) {
    this.saveSettings = data;
  }

  @action.bound
  clearSelectedRowData() {
    this.selectedRowData = [];
  }

  @action
  async setUiPreferences(userID: string | undefined, data: IUiPreferences): Promise<IUiPreferences> {
    return await this.service.setUiPreferences(userID, data);
  }

  @action
  async getUiPreferences(userID: string | undefined, name: string): Promise<IUiPreferences> {
    return await this.service.getUiPreferences(userID, name);
  }

  @action
  async setUiPreferencesOto(userID: string | undefined, data: IUiPreferences): Promise<IUiPreferences> {
    return await this.service.setUiPreferencesOto(userID, data);
  }

  @action
  async getUiPreferencesOto(userID: string | undefined, name: string): Promise<IUiPreferences> {
    return await this.service.getUiPreferencesOto(userID, name);
  }
}

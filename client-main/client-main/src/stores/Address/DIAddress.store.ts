import type { ILogger, IResponseService } from '@sber-sbertransport/mf-core';

import { inject, injectable } from 'inversify';
import { action, computed, observable } from 'mobx';

import { SYSTEM_MESSAGES } from 'constants/constants.app';

import { TYPES } from 'ioc/types';

import type { IAddressService, IAddressStore } from 'stores/Address/Address.interface';

import { plainToNew } from 'utils';

import { AddressModel } from './models/Address.model';
import { AddressNewModel } from './models/AddressNew.model';

@injectable()
export class DIAddressStore implements IAddressStore {
  @inject(TYPES.IAddressService)
  private service!: IAddressService;

  @inject(TYPES.ILogger)
  private logger!: ILogger;

  @inject(TYPES.IResponseService)
  private process!: IResponseService;

  @observable
    selfAddressList: AddressModel[] = [];

  @observable
    selfFrequentList: AddressModel[] = [];

  @observable
    selfFavoriteList: AddressModel[] = [];

  @observable
    selfCorporateList: AddressModel[] = [];

  @observable
    isAddressChanged: boolean | undefined = undefined;

  @computed
  get addressExistLabels(): string[] {
    return this.selfFavoriteList.map((x: AddressModel) => x.label).filter((x): x is string => typeof x === 'string');
  }

  @action.bound
    loadAddressList = async (): Promise<void> => {
      this.selfAddressList = (await this.getAddressList()) ?? [];
    };

  @action.bound
    loadCorporateList = async (): Promise<void> => {
      this.selfCorporateList = (await this.getCorporateList()) ?? [];
    };

  @action.bound
    loadFavoriteList = async (): Promise<void> => {
      this.selfFavoriteList = (await this.getFavoriteList()) ?? [];
    };

  @action.bound
    loadFrequentList = async (): Promise<void> => {
      this.selfFrequentList = (await this.getFrequentList()) ?? [];
    };

  @action.bound
    addFavoriteAddress = async (item: AddressNewModel): Promise<void> => {
      if (!this.checkLableUniqness(item.label)) {
        this.logger.toMessage('error', SYSTEM_MESSAGES.addressLableIsNotUniq);
        return;
      }

      if (this.isUnique(item)) {
        this.logger.toMessage('error', SYSTEM_MESSAGES.addressIsNotUniq);
        return;
      }

      const result = await this.createFavoriteAddress(item);

      if (result) {
        this.selfFavoriteList = [...this.selfFavoriteList, result];
        this.isAddressChanged = true;
        this.isAddressChanged = this.process.processStatus(200, SYSTEM_MESSAGES.addressAddSuccess);
      }
    };

  @action.bound
    deleteFrequentAddress = async (addressId: string): Promise<void> => {
      const result = await this.service.deleteFrequentAddress(addressId);
      this.isAddressChanged = this.process.processStatus(result, SYSTEM_MESSAGES.addressDeleteSuccess);

      if (result) {
        this.selfFrequentList = this.selfFrequentList.filter(x => x.id !== addressId);
      }
    };

  @action.bound
    deleteFavoriteAddress = async (addressId: string): Promise<void> => {
      const result = await this.service.deleteFavoriteAddress(addressId);
      this.isAddressChanged = this.process.processStatus(result, SYSTEM_MESSAGES.addressDeleteSuccess);

      if (result) {
        this.selfFavoriteList = this.selfFavoriteList.filter(x => x.id !== addressId);
      }
    };

  checkLableUniqness(label: string): boolean {
    return !this.addressExistLabels.includes(label);
  }

  isUnique(item: AddressNewModel): boolean {
    return this.selfFavoriteList.some(
      (el: AddressModel) => el.country === item.country
      && el.city === item.city
      && el.house === item.house
      && el.street === item.street
      && el.building === item.building
      && el.structure === item.structure
    );
  }

  private async getFrequentList(): Promise<AddressModel[] | undefined> {
    const result = await this.service.getFrequentList();
    return plainToNew<AddressModel[]>(AddressModel, result);
  }

  private async getAddressList(): Promise<AddressModel[] | undefined> {
    const result = await this.service.getAddressList();
    return plainToNew<AddressModel[]>(AddressModel, result);
  }

  private async getCorporateList(): Promise<AddressModel[] | undefined> {
    const result = await this.service.getCorporateList();
    return plainToNew<AddressModel[]>(AddressModel, result);
  }

  private async getFavoriteList(): Promise<AddressModel[] | undefined> {
    const result = await this.service.getFavoriteList();
    return plainToNew<AddressModel[]>(AddressModel, result);
  }

  private async createFavoriteAddress(item: AddressNewModel): Promise<AddressModel | undefined> {
    const result = await this.service.createFavoriteAddress(item);
    return plainToNew<AddressModel>(AddressModel, result);
  }

  initStore(): void {
    this.loadFavoriteList();
    this.loadFrequentList();
    this.loadCorporateList();
  }
}

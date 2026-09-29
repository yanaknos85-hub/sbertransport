import { inject, injectable } from 'inversify';
import { action, observable } from 'mobx';

import { TYPES } from 'ioc/types';

import type { CargoType } from 'types/Cargo';
import type { PageSetting } from 'shared/hooks/usePagination';
import type { ICargoListService, ICargoListStore } from './CargoList.interface';
import type { CargoListResponseType } from './CargoList.interface';

@injectable()
export class DICargoListStore implements ICargoListStore {
  @inject(TYPES.ICargoListService)
  private service!: ICargoListService;

  @observable
    cargoList: CargoListResponseType;

  @observable
    isModalVisible = false;

  @observable
    cargoId: string | null = null;

  @action.bound
  setModalVisible(visible: boolean) {
    this.isModalVisible = visible;
  }

  @action.bound
  setCargoId(cargoId: string): void {
    this.cargoId = cargoId;
  }

  @action.bound
  async getCargoListPersonal(pageSetting: PageSetting): Promise<void> {
    this.cargoList = await this.service.getCargoListPersonal(pageSetting).then(data => data);
  }

  @action.bound
  async deleteCargo(orgId: string, cargoId: string): Promise<any> {
    return this.service.deleteCargo(orgId, cargoId);
  }

  cargoTypeAutocompleteList: CargoType[];
}

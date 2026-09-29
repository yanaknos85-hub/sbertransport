import { inject, injectable } from 'inversify';
import { TYPES } from 'ioc/types';
import debounce from 'lodash/debounce';
import { action, observable } from 'mobx';

import type {
  CargoCreateType, CargoType, ICargoTypeService, ICargoTypeStore
} from './CargoType.interface';

@injectable()
export class DICargoTypeStore implements ICargoTypeStore {
  @inject(TYPES.ICargoTypeService)
  private service!: ICargoTypeService;

  @observable
    cargoType: CargoType | null = null;

  @observable
    cargoTypes: Record<string, CargoType> = {};

  @observable
    cargoTypeAutocompleteList: CargoType[] = [];

  @action
  clearState(): void {
    this.cargoType = null;
    this.cargoTypes = {};
    this.clearAutocompleteList();
  }

  private static calculateCargo(cargo: CargoType): CargoType {
    return {
      ...cargo,
      width: cargo.width / 10,
      length: cargo.length / 10,
      height: cargo.height / 10,
      // Объём приходит в мм3
      // Необходимо делить на 1000, чтобы отправлять данные объёма на бэк в сантиметрах
      volume: cargo.volume / 1000,
    };
  }

  @action.bound
  async onCargoTypeSelect(name: string, dontClear?: boolean): Promise<void> {
    const cargo = this.cargoTypeAutocompleteList.find(x => x.name === name);
    if (cargo) {
      const cargoType = DICargoTypeStore.calculateCargo(cargo);
      this.cargoType = cargoType;
      this.cargoTypes[cargoType.id] = cargoType;
    }
    if (!dontClear) {
      this.clearAutocompleteList();
    }
  }

  @action.bound
    searchCargoType = debounce(async (text: string, organizationId: string) => {
      this.cargoTypeAutocompleteList = observable(await this.service.searchCargoType(text, organizationId));
    }, 500);

  @action.bound
  async postCargoType(data: CargoCreateType, organizationId: string): Promise<void> {
    await this.service.postCargoType(data, organizationId);
  }

  private clearAutocompleteList(): void {
    this.cargoTypeAutocompleteList = [];
  }
}

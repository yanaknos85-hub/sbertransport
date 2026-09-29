import { inject, injectable } from 'inversify';
import debounce from 'lodash/debounce';
import { action, observable } from 'mobx';

import { TYPES } from 'ioc/types';

import type { ICargoTypeService, ICargoTypeStore } from './CargoType.interface';
import type { CargoCreateType, CargoType } from 'types/Cargo';

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
    searchCargoType = debounce(async (text: string) => {
      this.cargoTypeAutocompleteList = observable(await this.service.searchCargoType(text));
    }, 500);

  @action.bound
  async postCargoType(data: CargoCreateType): Promise<void> {
    await this.service.postCargoType(data);
  }

  private clearAutocompleteList(): void {
    this.cargoTypeAutocompleteList = [];
  }
}

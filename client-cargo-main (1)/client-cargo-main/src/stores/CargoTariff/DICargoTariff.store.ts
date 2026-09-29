import { inject, injectable } from 'inversify';
import { TYPES } from 'ioc/types';
import { action, observable } from 'mobx';

import { CargoRequestModel } from 'stores/Cargos/models/CargoRequest.model';

import type {
  CargoTypeCategoryNameEnum,
  ICargoTariffService,
  ICargoTariffStore,
  TariffCost,
  TariffRequest,
  TariffRequestMulti,
  TariffType
} from './CargoTariff.interface';

@injectable()
export class DICargoTariffStore implements ICargoTariffStore {
  @inject(TYPES.ICargoTariffService)
  private service!: ICargoTariffService;

  @observable
    tariff: TariffCost | null = null;

  @observable
    tariffMulti: TariffCost | null = null;

  @observable
    tariffs: Record<string, TariffCost> = {};

  @observable
    tariffsList: TariffCost[] = [];

  @observable
    tariffsListMulti: TariffCost[] = [];

  @observable
    cargoCategory: CargoTypeCategoryNameEnum | undefined;

  @action
  clearState(): void {
    this.tariff = null;
    this.tariffs = {};
    this.tariffsList = [];
    this.tariffsListMulti = [];
  }

  setTariff(tariff: TariffCost | null): void {
    this.tariff = tariff;
  }

  setTariffMulti(tariff: TariffCost | null): void {
    this.tariffMulti = tariff;
  }

  @action
  async calculateAllTariffs(data: TariffRequest): Promise<void> {
    const tariffs = (await this.service.calculateAllTariffs(data)) || [];

    this.tariffsList = tariffs;

    this.tariffs = tariffs.reduce(
      (acc, curr) => ({
        ...acc,
        [curr.transportType.name]: curr,
      }),
      {}
    );
  }

  @action.bound
  fillForSteps(cargoMultipleRequest: CargoRequestModel): void {
    if (!cargoMultipleRequest.calculatedTariff) {
      return;
    }
    this.cargoCategory = cargoMultipleRequest.listCargo?.[0]?.cargoCategory;
    this.tariffsListMulti = [cargoMultipleRequest.calculatedTariff];
  }

  @action
  async calculateAllTariffsMulti(data: TariffRequestMulti): Promise<void> {
    const tariffs = (await this.service.calculateAllTariffsMulti(data)) || [];

    if (tariffs.length > 0) {
      this.tariffsListMulti = tariffs
        .sort((a, b) => a.cost - b.cost)
        .reduce((acc: TariffCost[], cur, idx) => {
          const tariff = { ...cur, idx };
          return [...acc, tariff];
        }, []);

      this.tariffs = tariffs
        .sort((a, b) => a.cost - b.cost)
        .reduce(
          (acc: Record<string, TariffCost>, curr: TariffCost) => ({
            ...acc,
            [curr.transportType.name]: curr,
          }),
          {}
        );
    } else {
      this.tariffs = {};
      this.tariffsListMulti = [];
    }
  }

  @action
  async calculateTariff(data: TariffRequest, name: TariffType): Promise<void> {
    const [tariff] = (await this.service.calculateTariff(data, name)) || [];
    if (tariff) {
      this.setTariff(tariff);
    }
  }
}

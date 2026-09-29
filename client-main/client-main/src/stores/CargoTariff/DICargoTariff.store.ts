import { inject, injectable } from 'inversify';
import { action, observable } from 'mobx';

import { TYPES } from 'ioc/types';

import type {
  ICargoTariffService, ICargoTariffStore, TariffCost, TariffRequest, TariffType
} from './CargoTariff.interface';

@injectable()
export class DICargoTariffStore implements ICargoTariffStore {
  @inject(TYPES.ICargoTariffService)
  private service!: ICargoTariffService;

  @observable
    tariff: TariffCost | null = null;

  @observable
    tariffs: Record<string, TariffCost> = {};

  @observable
    tariffsList: TariffCost[] = [];

  @action
  clearState(): void {
    this.tariff = null;
    this.tariffs = {};
    this.tariffsList = [];
  }

  setTariff(tariff: TariffCost | null): void {
    this.tariff = tariff;
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

  @action
  async calculateTariff(data: TariffRequest, name: TariffType): Promise<void> {
    const [tariff] = (await this.service.calculateTariff(data, name)) || [];
    if (tariff) {
      this.setTariff(tariff);
    }
  }
}

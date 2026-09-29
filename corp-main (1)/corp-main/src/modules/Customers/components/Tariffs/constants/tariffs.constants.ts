import { TariffTypes } from 'constants/constants.app';

export enum TariffsTabs {
  Passengers = 'passengers',
  Cargo = 'cargo',
  CarService = 'carService',
}

export const tariffTypeTabs: TariffTypes[] = [TariffTypes.INCOME, TariffTypes.OUTCOME];

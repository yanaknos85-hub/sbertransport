import { ContractTypes } from 'constants/constants.app';

export enum ContractsTabs {
  Passengers = 'passengers',
  Cargo = 'cargo',
  CarService = 'carService',
  Ewb = 'ewb',
}

export const contractTypeTabs: ContractTypes[] = [ContractTypes.INCOME, ContractTypes.OUTCOME];

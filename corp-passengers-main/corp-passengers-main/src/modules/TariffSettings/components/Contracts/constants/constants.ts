// import { TariffsTabs } from 'modules/TariffSettings/constants';

// export const DEFAULT_SERVICE_TYPE = 'Перевозка сотрудников';
// export const DEFAULT_SERVICE_CARGO_TYPE = 'Грузоперевозки';
// export const DEFAULT_CAR_SERVICE_TYPE = 'Автосервис';

export const MIN_TEXT_INPUT = 0;
export const MAX_TEXT_INPUT = 100_000_000;

export const MIN_VAT_VALUE = 0;
export const MAX_VAT_VALUE = 100000000;

// export enum Modes {
//   Passengers = 'passengers',
//   Cargo = 'cargo',
//   CarService = 'carService',
// }

// export const serviceTypesDefaultValue = {
//   [TariffsTabs.Passengers]: { name: DEFAULT_SERVICE_TYPE, value: 'EMPLOYEE_TRANSPORTATION' },
//   [TariffsTabs.Cargo]: { name: DEFAULT_SERVICE_CARGO_TYPE, value: 'CARGO_TRANSPORTATION' },
//   [TariffsTabs.CarService]: { name: DEFAULT_CAR_SERVICE_TYPE, value: 'CAR_SERVICE_TRANSPORTATION' },
// };

export const serviceTypesDefaultValuePassengers = {
  name: 'Перевозка сотрудников',
  value: 'EMPLOYEE_TRANSPORTATION',
};

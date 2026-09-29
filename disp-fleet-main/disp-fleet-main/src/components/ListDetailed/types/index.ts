import { UUID } from 'utils/io-ts';

export type RowData = {
  id: UUID;
  rating?: number;
  [key: string]: unknown;
} & (
  | {
    lastName: string;
    firstName: string;
    patronymic?: string;
    stateNumber?: never;
  }
  | {
    stateNumber: string;
    lastName?: never;
    firstName?: never;
    patronymic?: never;
  }
);

export type StatusesType = 'active' | 'inLine' | 'inactive';

export enum IconTypes {
  PassengerCar = 'passengerCar',
  CargoTruck = 'cargoTruck',
  SpecialCar = 'specialCar',
  Avatar = 'avatar',
}

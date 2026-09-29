import { UUID } from 'utils/io-ts';

export type RowData = {
  id: UUID;
  [key: string]: unknown;
} & (
  | {
    lastName: string;
    firstName: string;
    patronymic?: string;
    stateNumber?: never;
    modelInfo?: never;
    name?: never;
  }
  | {
    stateNumber: string;
    modelInfo?: string;
    lastName?: never;
    firstName?: never;
    patronymic?: never;
    name?: never;
  }
  | {
    name: string;
    lastName?: never;
    firstName?: never;
    stateNumber?: never;
    modelInfo?: never;
    patronymic?: never;
  }
);

export type StatusesType = 'active' | 'inLine' | 'inactive' | 'inUse' | 'notInUse';

export type Query = Record<string, unknown>;

export enum RowIconTypes {
  PassengerCar = 'passengerCar',
  CargoTruck = 'cargoTruck',
  SpecialCar = 'specialCar',
  Avatar = 'avatar',
}

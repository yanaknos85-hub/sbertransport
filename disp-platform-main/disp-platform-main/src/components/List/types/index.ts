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
    name?: never;
  }
  | {
    stateNumber: string;
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
    patronymic?: never;
  }
);

export type StatusesType = 'active' | 'inLine' | 'inactive';

export type Query = Record<string, unknown>;

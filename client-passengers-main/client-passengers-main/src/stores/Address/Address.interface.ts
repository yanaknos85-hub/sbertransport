import * as t from 'io-ts';

import { IOWaypoint } from 'shared/models/geo/types';

import { AddressModel } from './models/Address.model';

export interface IAddressStore {
  selfAddressList: AddressModel[];
  selfFrequentList: AddressModel[];
  selfFavoriteList: AddressModel[];
  isAddressChanged: boolean | undefined;
  loadAddressList(): Promise<void>;
  loadFavoriteList(): Promise<void>;
  loadFrequentList(): Promise<void>;
  addFavoriteAddress(item: TAddressNew): Promise<void>;
  deleteFavoriteAddress(addressId: string): Promise<void>;
  deleteFrequentAddress(addressId: string): Promise<void>;
  isUnique(item: TAddressNew): boolean;

  initStore(): void;
}

export interface IAddressService {
  getAddressList(): Promise<TAddressExist[]>;
  getFrequentList(): Promise<TAddressExist[]>;
  getFavoriteList(): Promise<TAddressExist[]>;
  createFavoriteAddress(address: TAddressNew): Promise<TAddressExist>;
  getFavoriteAddress(addressId: string): Promise<TAddressExist>;
  updateFavoriteAddress(address: TAddressExist): Promise<number>;
  deleteFavoriteAddress(addressId: string): Promise<number>;
  deleteFrequentAddress(addressId: string): Promise<number>;
}

export const IOAddressNew = t.intersection([
  t.partial({
    label: t.string,
  }),
  IOWaypoint,
]);

export const IOAddressExist = t.intersection([t.type({ id: t.string }), t.partial({ usages: t.number }), IOAddressNew]);

export type TAddressNew = t.TypeOf<typeof IOAddressNew>;
export type TAddressExist = t.TypeOf<typeof IOAddressExist>;

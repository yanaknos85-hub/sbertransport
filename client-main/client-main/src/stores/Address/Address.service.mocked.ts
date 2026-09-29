import { injectable } from 'inversify';

import { IAddressService, TAddressExist } from 'stores/Address/Address.interface';

@injectable()
export class AddressServiceMocked implements IAddressService {
  getAddressList = async (): Promise<TAddressExist[]> => (await import('mock/stores/Address/getAddressList.json')).default as TAddressExist[];

  getFrequentList = async (): Promise<TAddressExist[]> => (await import('mock/stores/Address/getFrequentList.json')).default as TAddressExist[];

  getFavoriteList = async (): Promise<TAddressExist[]> => (await import('mock/stores/Address/getFavoriteList.json')).default as TAddressExist[];

  getCorporateList = async (): Promise<TAddressExist[]> => (await import('mock/stores/Address/getCorporateList.json')).default as TAddressExist[];

  createFavoriteAddress = async (): Promise<TAddressExist> => (await import('mock/stores/Address/createFavoriteAddress.json')).default as TAddressExist;

  getFavoriteAddress = async (): Promise<TAddressExist> => (await import('mock/stores/Address/getFavoriteAddress.json')).default as TAddressExist;

  updateFavoriteAddress = (): Promise<number> => Promise.resolve(200);

  deleteFavoriteAddress = (): Promise<number> => Promise.resolve(200);

  deleteFrequentAddress = (): Promise<number> => Promise.resolve(200);
}

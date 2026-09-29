import type { IHttpService, IResponseService } from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';
import { TYPES } from 'ioc/types';

import {
  SELF_ADDRESSES,
  SELF_FAVORITE_ADDRESSES,
  SELF_FAVORITE_ADDRESSES_PARAMS,
  SELF_FREQUENT_ADDRESSES,
  SELF_FREQUENT_ADDRESSES_PARAMS
} from 'constants/constants.env';

import {
  IAddressService, IOAddressExist, TAddressExist, TAddressNew
} from './Address.interface';

@injectable()
export class DIAddressService implements IAddressService {
  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.IResponseService)
  private process!: IResponseService;

  getAddressList(): Promise<TAddressExist[]> {
    return this.http
      .get<TAddressExist[]>(`${SELF_ADDRESSES}`)
      .then(x => this.process.getResponseData(x, IOAddressExist));
  }

  getFrequentList(): Promise<TAddressExist[]> {
    return this.http
      .get<TAddressExist[]>(`${SELF_FREQUENT_ADDRESSES}`)
      .then(x => this.process.getResponseData(x, IOAddressExist));
  }

  getFavoriteList(): Promise<TAddressExist[]> {
    return this.http
      .get<TAddressExist[]>(`${SELF_FAVORITE_ADDRESSES}`)
      .then(x => this.process.getResponseData(x, IOAddressExist));
  }

  createFavoriteAddress(address: TAddressNew): Promise<TAddressExist> {
    return this.http
      .post<TAddressExist>(`${SELF_FAVORITE_ADDRESSES}`, { ...address })
      .then(x => this.process.getResponseData(x, IOAddressExist));
  }

  getFavoriteAddress(addressId: string): Promise<TAddressExist> {
    return this.http
      .get<TAddressExist>(`${SELF_FAVORITE_ADDRESSES_PARAMS}`, { urlParams: { addressId } })
      .then(x => this.process.getResponseData(x, IOAddressExist));
  }

  updateFavoriteAddress(address: TAddressExist): Promise<number> {
    return this.http
      .put<TAddressExist[]>(
        `${SELF_FAVORITE_ADDRESSES_PARAMS}`,
        { ...address },
        { urlParams: { addressId: address.id } }
      )
      .then(this.process.getResponseStatus);
  }

  deleteFavoriteAddress(addressId: string): Promise<number> {
    return this.http
      .delete<TAddressExist[]>(`${SELF_FAVORITE_ADDRESSES_PARAMS}`, { urlParams: { addressId } })
      .then(this.process.getResponseStatus);
  }

  deleteFrequentAddress(addressId: string): Promise<number> {
    return this.http
      .delete<TAddressExist[]>(`${SELF_FREQUENT_ADDRESSES_PARAMS}`, { urlParams: { addressId } })
      .then(this.process.getResponseStatus);
  }
}

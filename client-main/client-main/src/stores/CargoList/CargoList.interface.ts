import * as t from 'io-ts';
import type { CargoType } from 'types/Cargo';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import { CargoTypeNameEnum } from 'types/Cargo';
import type { PageSetting } from 'shared/hooks/usePagination';

export enum CargoTypeCategoryNameEnum {
  REGULAR = 'REGULAR',
  LIQUID = 'LIQUID',
  BULK = 'BULK',
  CORRESPONDENCE = 'CORRESPONDENCE',
}

export enum AccessControlEnum {
  PUBLIC = 'PUBLIC',
  PERSONAL = 'PERSONAL',
  ORGANIZATION = 'ORGANIZATION',
}

export const cargoPersonalListItem = t.type({
  id: t.string,
  name: t.string,
  type: ioTypeFromEnum('type', CargoTypeNameEnum),
  category: ioTypeFromEnum('category', CargoTypeCategoryNameEnum),
  length: t.number,
  width: t.number,
  height: t.number,
  weight: t.number,
  volume: t.number,
  accessLevel: ioTypeFromEnum('accessLevel', AccessControlEnum),
});

export type CargoPersonalListItemType = t.TypeOf<typeof cargoPersonalListItem>;

const Pageable = t.type({
  pageNumber: t.number,
  pageSize: t.number,
  sort: t.type({
    empty: t.boolean,
    sorted: t.boolean,
    unsorted: t.boolean,
  }),
  offset: t.number,
  paged: t.boolean,
  unpaged: t.boolean,
});

export const CargoListResponse = t.type({
  content: t.array(cargoPersonalListItem),
  pageable: Pageable,
  totalElements: t.number,
  totalPages: t.number,
  last: t.boolean,
  size: t.number,
  number: t.number,
  sort: t.type({
    empty: t.boolean,
    sorted: t.boolean,
    unsorted: t.boolean,
  }),
  numberOfElements: t.number,
  first: t.boolean,
  empty: t.boolean,
});

export type CargoListResponseType = t.TypeOf<typeof CargoListResponse>;

export interface ICargoListStore {
  cargoTypeAutocompleteList: CargoType[];
  cargoId: string;
  isModalVisible: boolean;
  cargoList: CargoListResponseType;
  setCargoId(cargoId: string): void;
  setModalVisible(visible: boolean): void;
  getCargoListPersonal(pageSetting: PageSetting): void;
  deleteCargo(orgId: string, cargoId: string): Promise<void>;
}

export interface ICargoListService {
  getCargoListPersonal(pageSetting: PageSetting): Promise<CargoListResponseType>;
  deleteCargo(orgId: string, cargoId: string): Promise<any>;
}

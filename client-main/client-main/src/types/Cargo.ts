import * as t from 'io-ts';

import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

export enum CargoTypeNameEnum {
  OTHER = 'OTHER',
  TECHNIQUE = 'TECHNIQUE',
  DOCUMENT = 'DOCUMENT',
  TABLEWARE = 'TABLEWARE',
  CLOTHES = 'CLOTHES',
  FURNITURE = 'FURNITURE',
  FOOD_PRODUCTS = 'FOOD_PRODUCTS',
  TOOLS = 'TOOLS',
  CUNSTRUCTION_MATERIALS = 'CUNSTRUCTION_MATERIALS',
  HOUSEHOLD_GOODS = 'HOUSEHOLD_GOODS',
}

export const cargoTypeNameTitle = {
  [CargoTypeNameEnum.OTHER]: 'Другое',
  [CargoTypeNameEnum.TECHNIQUE]: 'Техника',
  [CargoTypeNameEnum.DOCUMENT]: 'Документы',
  [CargoTypeNameEnum.TABLEWARE]: 'Посуда',
  [CargoTypeNameEnum.CLOTHES]: 'Одежда',
  [CargoTypeNameEnum.FURNITURE]: 'Мебель',
  [CargoTypeNameEnum.FOOD_PRODUCTS]: 'Продовольствие',
  [CargoTypeNameEnum.TOOLS]: 'Инструменты',
  [CargoTypeNameEnum.CUNSTRUCTION_MATERIALS]: 'Строительные материалы',
  [CargoTypeNameEnum.HOUSEHOLD_GOODS]: 'Хозяйственные товары',
};

export enum CargoTypeCategoryNameEnum {
  OTHER = 'OTHER',
  REGULAR = 'REGULAR',
  LIQUID = 'LIQUID',
  BULK = 'BULK',
  CORRESPONDENCE = 'CORRESPONDENCE',
}

export const cargoTypeCategoryNameTitle = {
  [CargoTypeCategoryNameEnum.REGULAR]: 'Обычные',
  [CargoTypeCategoryNameEnum.LIQUID]: 'Наливные грузы',
  [CargoTypeCategoryNameEnum.BULK]: 'Насыпные грузы',
  [CargoTypeCategoryNameEnum.CORRESPONDENCE]: 'Корреспонденция',
};

export enum TransportTypeEnum {
  DEDICATED = 'DEDICATED',
  COURIER = 'COURIER',
  INTERREGIONAL = 'INTERREGIONAL',
}

export const transportTypeTitles = {
  [TransportTypeEnum.DEDICATED]: 'Грузовик',
  [TransportTypeEnum.COURIER]: 'Курьер',
  [TransportTypeEnum.INTERREGIONAL]: 'Между регионами',
};

export enum CargoCategoryEnum {
  OTHER = 'OTHER',
}

export type TransportType = TransportTypeEnum;

export interface CargoType {
  id: string;
  name: string;
  type: CargoTypeNameEnum;
  category: CargoTypeCategoryNameEnum;
  length: number;
  width: number;
  height: number;
  weight: number;
  volume: number;
  active?: boolean;
}

export const cargoType = t.intersection([
  t.type({
    id: t.string,
    name: t.string,
    type: ioTypeFromEnum('CargoName', CargoTypeNameEnum),
    category: ioTypeFromEnum('CargoCategory', CargoTypeCategoryNameEnum),
    length: t.number,
    width: t.number,
    height: t.number,
    weight: t.number,
    volume: t.number,
  }),
  t.partial({
    active: t.boolean,
  }),
]);

export type CargoCreateType = Omit<CargoType, 'id'>;

export interface Point {
  country: string;
  region: string;
  city: string;
  street: string;
  house: number;
  latitude: number;
  longitude: number;
  existInVspGosbTbRegistry?: string;
}

const Sizes = t.type({
  width: t.number,
  length: t.number,
  height: t.number,
  volume: t.number,
  weight: t.number,
});

export type Sizes = t.TypeOf<typeof Sizes>;

export const cargoListItem = t.intersection([
  Sizes,
  t.type({
    id: t.string,
    position: t.number,
    cargoName: t.string,
    cargoType: ioTypeFromEnum('cargoType', CargoTypeNameEnum),
    cargoCategory: ioTypeFromEnum('cargoCategory', CargoTypeCategoryNameEnum),
    occupiedPlacesCount: t.number,
  }),
  t.partial({
    fragile: t.boolean,
    needPackage: t.boolean,
    packageCount: t.number,
    image: t.string,
    cargoTypeValue: t.string,
  }),
]);

export type CargoListItem = t.TypeOf<typeof cargoListItem> & {};

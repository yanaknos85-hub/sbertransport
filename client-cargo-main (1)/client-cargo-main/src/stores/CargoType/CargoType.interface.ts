import * as t from 'io-ts';

import { ioTypeFromEnum } from '../../utils/ioTypeFromEnum';

export enum CargoTypeNameEnum {
  OTHER = 'OTHER',
  TECHNIQUE = 'TECHNIQUE',
  DOCUMENT = 'DOCUMENT',
  DOCUMENT_CARS = 'DOCUMENT_CARS',
  TABLEWARE = 'TABLEWARE',
  CLOTHES = 'CLOTHES',
  FURNITURE = 'FURNITURE',
  FOOD_PRODUCTS = 'FOOD_PRODUCTS',
  TOOLS = 'TOOLS',
  MATERIALS = 'MATERIALS',
  HOUSEHOLD_GOODS = 'HOUSEHOLD_GOODS',
}

export const cargoTypeNameTitle = {
  [CargoTypeNameEnum.OTHER]: 'Другое',
  [CargoTypeNameEnum.TECHNIQUE]: 'Техника',
  [CargoTypeNameEnum.DOCUMENT]: 'Документы',
  [CargoTypeNameEnum.DOCUMENT_CARS]: 'Документы ЦАРС',
  [CargoTypeNameEnum.TABLEWARE]: 'Посуда',
  [CargoTypeNameEnum.CLOTHES]: 'Одежда',
  [CargoTypeNameEnum.FURNITURE]: 'Мебель',
  [CargoTypeNameEnum.FOOD_PRODUCTS]: 'Продовольствие',
  [CargoTypeNameEnum.TOOLS]: 'Инструменты',
  [CargoTypeNameEnum.MATERIALS]: 'Строительные материалы',
  [CargoTypeNameEnum.HOUSEHOLD_GOODS]: 'Хозяйственные товары',
};

export enum CargoTypeCategoryNameEnum {
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

export interface ICargoTypeStore {
  cargoType: CargoType | null;
  cargoTypes: Record<string, CargoType>;
  cargoTypeAutocompleteList: CargoType[];
  clearState(): void;
  onCargoTypeSelect(name: string, dontClear?: boolean): void;
  searchCargoType(text: string, organizationId: string): void;
  postCargoType(data: CargoCreateType, organizationId: string): Promise<void>;
}

export interface ICargoTypeService {
  searchCargoType(text: string, organizationId: string): Promise<CargoType[]>;
  postCargoType(data: CargoCreateType, organizationId: string): Promise<unknown>;
}

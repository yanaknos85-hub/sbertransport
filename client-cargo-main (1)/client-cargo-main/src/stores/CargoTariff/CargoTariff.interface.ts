import * as t from 'io-ts';

import { Point, TransportTypeEnum } from 'types/Cargo';

import { ioTypeFromEnum } from '../../utils/ioTypeFromEnum';

export const TransportType = TransportTypeEnum;

export type TariffType = TransportTypeEnum;

export enum CargoTypeCategoryNameEnum {
  REGULAR = 'REGULAR',
  LIQUID = 'LIQUID',
  BULK = 'BULK',
  CORRESPONDENCE = 'CORRESPONDENCE',
  OTHER = 'OTHER',
}

export interface PriceDetails {
  baseCost: number;
  expressCost: number;
  loaderCost: number;
  distance?: number;
  hourTariff?: number;
  includedHours?: number;
  packageCost?: number;
}

interface Auto {
  capacity: {
    capacity: number;
    id: string;
  };
  height: number;
  id: string ;
  length: number;
  name: string;
  volume: number;
  width: number;
}

/**
 * @param loaders Общее количество грузчиков на последнем шаге
 */
export interface TariffCost {
  auto?: Auto;
  active?: boolean;
  cargoCategory?: string;
  contragent?: string;
  contragentId?: string;
  cost: number;
  deliveryTime: number;
  distance?: number;
  humanReadableId?: string;
  id: string;
  idx?: number;
  priceDetails: PriceDetails;
  workGroup?: string;
  transportType: {
    id: string;
    name: TariffType;
    nameRus: string;
  };
  loaders: number;
}

/**
 * @param loaders Общее количество грузчиков на последнем шаге
 */
export const tariffCost = t.intersection([
  t.type({
    id: t.string,
    cost: t.number,
    deliveryTime: t.number,
    priceDetails: t.type({
      baseCost: t.number,
      expressCost: t.number,
      loaderCost: t.number,
    }),
    transportType: t.type({
      id: t.string,
      name: ioTypeFromEnum('TransportTypeEnum', TransportTypeEnum),
      nameRus: t.string,
    }),
    loaders: t.number,
  }),
  t.partial({
    active: t.boolean,
  }),
]);

export interface Package {
  count: number;
  id?: string;
  name?: string;
}

export interface PackageTariff {
  id: string;
  cost: number;
}

export interface TariffRequest {
  organizationId: string;
  time: number;
  weight: number;
  volume: number;
  distance: number;
  startPoint: Point;
  stopPoint?: Point;
  countPoint: number;
  express: boolean;
  priceDetails?: any;
}

interface TransportType {
  id: string;
  name: TariffType;
  nameRus: string;
}

interface PriceDetailsMulti {
  baseTariff: number;
  baseCost: number;
  loaderTariff: number;
  loaderCost: number;
  packageTariff: PackageTariff[];
  packageCost: number;
  includedHours: number;
  hourTariff: number;
  expressCost: number;
  transportType: TransportType;
}

export enum PurposeEnum {
  REGULAR = 'REGULAR',
  RELOCATION = 'RELOCATION',
  STANDART = 'STANDART',
}

export const Purpose = {
  [PurposeEnum.REGULAR]: 'REGULAR',
  [PurposeEnum.RELOCATION]: 'RELOCATION',
  [PurposeEnum.STANDART]: 'STANDART',
};

export interface TariffRequestMulti {
  organizationId: string;
  time: number;
  weight: number;
  volume: number;
  distance: number;
  startPoint: Point;
  stopPoint?: Point;
  express: boolean;
  countPoint: number;
  loadersNeeded: boolean;
  loaders?: number;
  maxWeightOfOnePlace: number;
  occupiedPlacesCount: number;
  packages?: Package[];
  tripDate?: string;
  cargoCategory?: CargoTypeCategoryNameEnum;
  includeTemplate: boolean;
  calcType?: PurposeEnum | undefined;
}

export interface TariffCostMulti {
  id: string;
  cost: number;
  priceDetails: PriceDetailsMulti;
  deliveryTime: number;
  active?: boolean;
  contragent: string;
  contragentId: string;
  distance: number;
  transportType: TransportType;
}

export interface ICargoTariffStore {
  tariff: TariffCost | null;
  tariffs: Record<string, TariffCost>;
  tariffsList: TariffCost[];
  tariffsListMulti: TariffCost[];
  setTariff(tariff: TariffCost | null): void;
  setTariffMulti(tariff: TariffCost | null): void;
  clearState(): void;
  calculateTariff(data: TariffRequest, type: TariffType): Promise<void>;
  calculateAllTariffs(data: TariffRequest): Promise<void>;
  calculateAllTariffsMulti(data: TariffRequestMulti): Promise<void>;
}

export interface ICargoTariffService {
  calculateTariff(data: TariffRequest, type: TariffType): Promise<TariffCost[]>;
  calculateAllTariffs(data: TariffRequest): Promise<TariffCost[]>;
  calculateAllTariffsMulti(data: TariffRequestMulti): Promise<TariffCost[]>;
}

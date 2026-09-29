import * as t from 'io-ts';

import { Point, TransportTypeEnum } from 'types/Cargo';

import { ioTypeFromEnum } from '../../utils/ioTypeFromEnum';

export const TransportType = TransportTypeEnum;

export type TariffType = TransportTypeEnum;

export interface PriceDetails {
  baseCost: number;
  expressCost: number;
  loaderCost: number;
}

export interface TariffCost {
  id: string;
  cost: number;
  priceDetails: PriceDetails;
  time: number;
  active?: boolean;
  transportType: {
    id: string;
    name: TariffType;
  };
}

export const tariffCost = t.intersection([
  t.type({
    id: t.string,
    cost: t.number,
    time: t.number,
    priceDetails: t.type({
      baseCost: t.number,
      expressCost: t.number,
      loaderCost: t.number,
    }),
    transportType: t.type({
      id: t.string,
      name: ioTypeFromEnum('TransportTypeEnum', TransportTypeEnum),
    }),
  }),
  t.partial({
    active: t.boolean,
  }),
]);

export interface TariffRequest {
  organizationId: string;
  time: number;
  weight: number;
  distance: number;
  startPoint: Point;
  stopPoint?: Point;
  countPoint: number;
  express: boolean;
  priceDetails?: any;
}

export interface ICargoTariffStore {
  tariff: TariffCost | null;
  tariffs: Record<string, TariffCost>;
  tariffsList: TariffCost[];
  setTariff(tariff: TariffCost | null): void;
  clearState(): void;
  calculateTariff(data: TariffRequest, type: TariffType): Promise<void>;
  calculateAllTariffs(data: TariffRequest): Promise<void>;
}

export interface ICargoTariffService {
  calculateTariff(data: TariffRequest, type: TariffType): Promise<TariffCost[]>;
  calculateAllTariffs(data: TariffRequest): Promise<TariffCost[]>;
}

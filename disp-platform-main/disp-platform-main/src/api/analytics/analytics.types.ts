import * as t from 'io-ts';
import { AnalyticsTransportType } from './analytics.constants';

export interface AutoAnalyticsFilters {
  contractorIds?: string[];
  autoparkIds?: string[];
  year: number;
  months?: number[];
  stateNumbers?: string[];
  brands?: string[];
}

export const VehicleMonthData = t.strict({
  /** Месяц 1-12 */
  month: t.number,
  inExploitationCount: t.number,
  notInExploitationCount: t.number,
});

export const VehicleAnalyticsResponse = t.array(VehicleMonthData);

export type VehicleAnalyticsResponse = t.TypeOf<typeof VehicleAnalyticsResponse>;

export const RepairMonthData = t.strict({
  /** Месяц 1-12 */
  month: t.number,
  day: t.number,
  inWorkCount: t.number,
  inRepairCount: t.number,
});

export const RepairAnalyticsResponse = t.array(RepairMonthData);

export type RepairAnalyticsResponse = t.TypeOf<typeof RepairAnalyticsResponse>;

export const MileageCostMonthData = t.strict({
  /** Месяц 1-12 */
  month: t.number,
  mileageMonthCost: t.number,
});

export const MileageCostResponse = t.type({
  fuel: t.array(MileageCostMonthData),
  maintenance: t.array(MileageCostMonthData),
});

export type MileageCostResponse = t.TypeOf<typeof MileageCostResponse>;

export const FuelConsumptionMonthData = t.strict({
  /** Месяц 1-12 */
  month: t.number,
  consumption: t.number,
});

export const FuelConsumptionResponse = t.type({
  liters: t.array(FuelConsumptionMonthData),
  rubles: t.array(FuelConsumptionMonthData),
});

export type FuelConsumptionResponse = t.TypeOf<typeof FuelConsumptionResponse>;

export interface AnalyticsTransportFilters {
  data: AnalyticsTransportType;
  contractorIds?: string[];
  autoparkIds?: string[];
}

export const OneVehicleExploitation = t.type({
  exploitationStart: t.string,
  inExploitationDays: t.number,
});

export type OneVehicleExploitation = t.TypeOf<typeof OneVehicleExploitation>;

export interface OneVehicleExploitationFilters {
  stateNumber: string;
}

export const OneVehicleRepair = t.type({
  month: t.number,
  repairs: t.array(t.type({
    start: t.string,
    end: t.string,
  })),
});

export type OneVehicleRepair = t.TypeOf<typeof OneVehicleRepair>;

export interface OneVehicleRepairFilters {
  stateNumber: string;
  year: number;
  months?: number[];
}

/** Статистика по брендам */
export const BrandStatistics = t.type({
  /** Месяц */
  month: t.number,
  /** Общее количество */
  totalCount: t.number,
  /** Статистика по брендам */
  brands: t.array(t.type({
    /** Бренд, марка */
    brand: t.string,
    /** Количество */
    count: t.number,
  })),
});

export type BrandStatistics = t.TypeOf<typeof BrandStatistics>;

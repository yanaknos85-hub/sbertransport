import { QueryConfig } from 'react-query';
import * as t from 'io-ts';

import { APIQueryResult, useAPI } from 'api';
import {
  ANALYTICS_TRANSPORT,
  BRAND_STATISTICS,
  FUEL_CONSUMPTION_ANALYTICS, MILEAGE_COST_ANALYTICS, ONE_VEHICLE_ANALYTICS, REPAIR_ANALYTICS, VEHICLE_ANALYTICS
} from './analytics.constants';

import {
  AutoAnalyticsFilters, VehicleAnalyticsResponse, RepairAnalyticsResponse,
  MileageCostResponse,
  FuelConsumptionResponse,
  AnalyticsTransportFilters,
  OneVehicleExploitationFilters,
  OneVehicleExploitation,
  OneVehicleRepairFilters,
  OneVehicleRepair,
  BrandStatistics
} from './analytics.types';

const VEHICLE_ANALYTICS_KEY = 'vehicle-analytics';
const ONE_VEHICLE_ANALYTICS_KEY = 'one-vehicle-analytics';
const REPAIR_ANALYTICS_KEY = 'repair-analytics';
const ONE_REPAIR_ANALYTICS_KEY = 'one-repair-analytics';
const MILEAGE_COST_ANALYTICS_KEY = 'mileage-cost-analytics';
const FUEL_CONSUMPTION_ANALYTICS_KEY = 'fuel-consumption-analytics';
const BRANDS_ANALYTICS_KEY = 'brands-analytics';
const ANALYTICS_TRANSPORT_KEY = 'analytics-transport';

declare module 'api' {
  interface Cache {
    vehicleAnalytics: {
      key: [typeof VEHICLE_ANALYTICS_KEY, AutoAnalyticsFilters];
      value: VehicleAnalyticsResponse;
    };
    oneVehicleAnalytics: {
      key: [typeof ONE_VEHICLE_ANALYTICS_KEY, OneVehicleExploitationFilters];
      value: OneVehicleExploitation;
    };
    repairAnalytics: {
      key: [typeof REPAIR_ANALYTICS_KEY, AutoAnalyticsFilters];
      value: RepairAnalyticsResponse;
    };
    oneRepairAnalytics: {
      key: [typeof ONE_REPAIR_ANALYTICS_KEY, OneVehicleRepairFilters];
      value: OneVehicleRepair[];
    };
    mileageCostAnalytics: {
      key: [typeof MILEAGE_COST_ANALYTICS_KEY, AutoAnalyticsFilters];
      value: MileageCostResponse;
    };
    fuelConsumptionAnalytics: {
      key: [typeof FUEL_CONSUMPTION_ANALYTICS_KEY, AutoAnalyticsFilters];
      value: FuelConsumptionResponse;
    };
    brandsAnalytics: {
      key: [typeof BRANDS_ANALYTICS_KEY, AutoAnalyticsFilters];
      value: BrandStatistics[];
    };
    analyticsTransport: {
      key: [typeof ANALYTICS_TRANSPORT_KEY, AnalyticsTransportFilters];
      value: string[];
    };
  }
}

/** Получение количества эксплуатируемых и неэксплуатируемых авто */
export const useVehicleAnalytics = (
  data: AutoAnalyticsFilters,
  config?: QueryConfig<VehicleAnalyticsResponse, Error>
): APIQueryResult<VehicleAnalyticsResponse, Error> => useAPI(
  [VEHICLE_ANALYTICS_KEY, data],
  ({ http, process }) => http
    .post<VehicleAnalyticsResponse>(VEHICLE_ANALYTICS, data)
    .then(process.decodeResponseData(VehicleAnalyticsResponse)),
  {
    keepPreviousData: true,
    ...config,
  }
);

/** Получение информации об эксплуатации конкретного автомобиля */
export const useOneVehicleExploitation = (
  { stateNumber }: OneVehicleExploitationFilters,
  config?: QueryConfig<OneVehicleExploitation, Error>
): APIQueryResult<OneVehicleExploitation, Error> => useAPI(
  [ONE_VEHICLE_ANALYTICS_KEY, { stateNumber }],
  ({ http, process }) => http
    .get<OneVehicleExploitation>(ONE_VEHICLE_ANALYTICS, { urlParams: { stateNumber } })
    .then(process.decodeResponseData(OneVehicleExploitation)),
  {
    keepPreviousData: true,
    ...config,
  }
);

/** Получение количества ремонтируемых авто */
export const useRepairAnalytics = (
  data: AutoAnalyticsFilters,
  config?: QueryConfig<RepairAnalyticsResponse, Error>
): APIQueryResult<RepairAnalyticsResponse, Error> => useAPI(
  [REPAIR_ANALYTICS_KEY, data],
  ({ http, process }) => http
    .post<RepairAnalyticsResponse>(REPAIR_ANALYTICS, data)
    .then(process.decodeResponseData(RepairAnalyticsResponse)),
  {
    keepPreviousData: true,
    ...config,
  }
);

/** Получение информации о ремонтах конкретного авто */
export const useOneRepairAnalytics = (
  { stateNumber, ...params }: OneVehicleRepairFilters,
  config?: QueryConfig<OneVehicleRepair[], Error>
): APIQueryResult<OneVehicleRepair[], Error> => useAPI(
  [ONE_REPAIR_ANALYTICS_KEY, { stateNumber, ...params }],
  ({ http, process }) => http
    .get<OneVehicleRepair[]>(ONE_REPAIR_ANALYTICS_KEY, { urlParams: { stateNumber }, params })
    .then(process.decodeResponseData(t.array(OneVehicleRepair))),
  {
    keepPreviousData: true,
    ...config,
  }
);

/** Получение суммы расходов на содержание автомобилей */
export const useMileageCostAnalytics = (
  data: AutoAnalyticsFilters,
  config?: QueryConfig<MileageCostResponse, Error>
): APIQueryResult<MileageCostResponse, Error> => useAPI(
  [MILEAGE_COST_ANALYTICS_KEY, data],
  ({ http, process }) => http
    .post<MileageCostResponse>(MILEAGE_COST_ANALYTICS, data)
    .then(process.decodeResponseData(MileageCostResponse)),
  {
    keepPreviousData: true,
    ...config,
  }
);

/** Получение данных о расходе топлива */
export const useFuelConsumptionAnalytics = (
  data: AutoAnalyticsFilters,
  config?: QueryConfig<FuelConsumptionResponse, Error>
): APIQueryResult<FuelConsumptionResponse, Error> => useAPI(
  [FUEL_CONSUMPTION_ANALYTICS_KEY, data],
  ({ http, process }) => http
    .post<FuelConsumptionResponse>(FUEL_CONSUMPTION_ANALYTICS, data)
    .then(process.decodeResponseData(FuelConsumptionResponse)),
  {
    keepPreviousData: true,
    ...config,
  }
);

/** Получение данных о доступном транспорте */
export const useAnalyticsTransport = (
  filters: AnalyticsTransportFilters,
  config?: QueryConfig<string[], Error>
): APIQueryResult<string[], Error> => useAPI(
  [ANALYTICS_TRANSPORT_KEY, filters],
  ({ http, process }) => {
    const { data, ...body } = filters;

    return http
      .post<string[]>(ANALYTICS_TRANSPORT, body, { params: { data } })
      .then(process.decodeResponseData(t.array(t.string)));
  },
  {
    keepPreviousData: true,
    ...config,
  }
);

/** Получение статистики по брендам */
export const useBrandsStatistics = (
  filters: AutoAnalyticsFilters,
  config?: QueryConfig<BrandStatistics[], Error>
): APIQueryResult<BrandStatistics[], Error> => useAPI(
  [BRANDS_ANALYTICS_KEY, filters],
  ({ http, process }) => {
    return http
      .post<BrandStatistics[]>(BRAND_STATISTICS, filters)
      .then(process.decodeResponseData(t.array(BrandStatistics)));
  },
  {
    keepPreviousData: true,
    ...config,
  }
);

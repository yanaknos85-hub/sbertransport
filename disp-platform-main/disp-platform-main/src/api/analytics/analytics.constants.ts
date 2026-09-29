const ANALYTICS = '/auto-analytics';

export const VEHICLE_ANALYTICS = `${ANALYTICS}/autopark-statistics/exploitation`;
export const ONE_VEHICLE_ANALYTICS = `${ANALYTICS}/autopark-statistics/exploitation/:stateNumber`;
export const REPAIR_ANALYTICS = `${ANALYTICS}/autopark-statistics/repair`;
export const ONE_REPAIR_ANALYTICS = `${ANALYTICS}/autopark-statistics/repair/:stateNumber`;
export const BRAND_STATISTICS = `${ANALYTICS}/autopark-statistics/brands`;

export const MILEAGE_COST_ANALYTICS = `${ANALYTICS}/mileage-cost`;
export const FUEL_CONSUMPTION_ANALYTICS = `${ANALYTICS}/fuel-consumption`;

export const ANALYTICS_TRANSPORT = `${ANALYTICS}/transport`;

export enum AnalyticsTransportType {
  BRAND_NAME = 'BRAND_NAME',
  STATE_NUMBER = 'STATE_NUMBER',
}

export const analyticsTransportType = {
  [AnalyticsTransportType.BRAND_NAME]: 'По марке',
  [AnalyticsTransportType.STATE_NUMBER]: 'По госномеру',
};

export const subtypesText = {
  [AnalyticsTransportType.BRAND_NAME]: 'Все марки',
  [AnalyticsTransportType.STATE_NUMBER]: 'Все номера',
};

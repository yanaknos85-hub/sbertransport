import moment from 'moment';

import {
  FuelConsumptionResponse,
  MileageCostResponse,
  RepairAnalyticsResponse,
  VehicleAnalyticsResponse
} from 'api/analytics/analytics.types';
import { BarChartData } from '../components/BarChart';
import { IndicatorTypes } from '../charts/FuelConsumptionAnalytics/FuelConsumptionAnalytics';

export const formatVehicleToChartData = (response: VehicleAnalyticsResponse, isTotal?: boolean): BarChartData[] => {
  return Array.from({ length: 12 }).map((_, index) => {
    const month = index + 1;
    const data = response.find(x => x.month === month);

    const primary = data?.inExploitationCount || 0;
    const secondary = data?.notInExploitationCount || 0;

    return {
      date: month,
      primary: isTotal ? primary + secondary : primary,
      secondary,
    };
  });
};

export const formatRepairToChartData = (
  response: RepairAnalyticsResponse,
  selectedMonth: number,
  selectedYear: number
): BarChartData[] => {
  const daysInSelectedMonth = moment(`${selectedYear}-${selectedMonth + 1}`, 'YYYY-M').daysInMonth();

  return Array.from({ length: daysInSelectedMonth }).map((_, index) => {
    const day = index + 1;

    const data = response
      .filter(x => x.month === selectedMonth + 1)
      .find(x => x.day === day);

    return {
      date: day,
      selectedMonth,
      primary: data?.inWorkCount || 0,
      secondary: data?.inRepairCount || 0,
    };
  });
};

export const formatMileageCostToChartData = (response: MileageCostResponse): BarChartData[] => {
  const { fuel = [], maintenance = [] } = response || {};

  const indexedFuel = fuel.reduce((acc, item) => {
    acc[item.month] = item.mileageMonthCost;
    return acc;
  }, {} as Record<number, number>);

  const indexedMaintenance = maintenance.reduce((acc, item) => {
    acc[item.month] = item.mileageMonthCost;
    return acc;
  }, {} as Record<number, number>);

  return Array.from({ length: 12 }).map((_, index) => {
    const month = index + 1;

    const primary = indexedFuel[month] || 0;
    const secondary = indexedMaintenance[month] || 0;

    return {
      date: month,
      primary,
      secondary,
    };
  });
};

export const formatFuelConsumptionToChartData = (
  response: FuelConsumptionResponse,
  type: IndicatorTypes
): BarChartData[] => {
  const { liters = [], rubles = [] } = response || {};

  const indexedLiters = liters.reduce((acc, item) => {
    acc[item.month] = item.consumption;
    return acc;
  }, {} as Record<number, number>);

  const indexedRubles = rubles.reduce((acc, item) => {
    acc[item.month] = item.consumption;
    return acc;
  }, {} as Record<number, number>);

  return Array.from({ length: 12 }).map((_, index) => {
    const month = index + 1;

    const primary = indexedLiters[month] || 0;
    const secondary = indexedRubles[month] || 0;

    return {
      date: month,
      primary: type === IndicatorTypes.LITERS ? primary : secondary,
      secondary: 0,
    };
  });
};

import * as R from 'ramda';

import type { YandexTaxiReportFilter } from 'api/yandexTaxiRegistry/yandex-taxi-registry.types';
import type { reportParams, TaxiYandexRegistryFilter } from '../types/types';
import type { Waypoint } from 'stores/Geo/Geo.interface';
import { toDateRangeISO } from 'shared/components/DateInput/utils';
import { clearSymbols } from 'utils/Misc';
import { formattedTimezoneToUTC } from 'utils/times';

export const processYandexTaxiRequestQueryOnXlsDownload = (
  { filters, withFilters }: reportParams,
  organizationId: string
) => {
  if (filters && withFilters) {
    const desiredDateRange = filters.desiredDateRange ? toDateRangeISO(filters.desiredDateRange) : undefined;
    const paymentFormationRange = filters.orderPaymentFormationStartRange
      ? toDateRangeISO(filters.orderPaymentFormationStartRange)
      : undefined;
    const preparedFilters = {
      humanReadableId: filters.humanReadableId,
      status: filters?.status?.length ? filters?.status.join(',') : undefined,
      passengerName: filters.passengerFullName ? clearSymbols(filters.passengerFullName).trim() : undefined,
      approverName: filters.approverFullName ? clearSymbols(filters.approverFullName).trim() : undefined,
      paymentFormationStart: paymentFormationRange ? paymentFormationRange.start : undefined,
      paymentFormationEnd: paymentFormationRange ? paymentFormationRange.end : undefined,
      startFrom: desiredDateRange ? desiredDateRange.start : undefined,
      startTo: desiredDateRange ? desiredDateRange.end : undefined,
      timeZone: formattedTimezoneToUTC(),
      costCenter: filters?.costCenter,
      balanceUnitSet: filters?.balanceUnits?.join(',') || undefined,
      organizationId,
    };

    return R.reject(x => typeof x === 'undefined' || x === '')(preparedFilters);
  }

  return { organizationId };
};

export const processPaymentYandexTaxiRequestQueryOnXlsDownload = (
  { filters, withFilters }: reportParams,
  organizationId: string
) => {
  if (filters && withFilters) {
    const desiredDateRange = filters.desiredDateRange ? toDateRangeISO(filters.desiredDateRange) : undefined;
    const paymentFormationRange = filters.orderPaymentFormationStartRange
      ? toDateRangeISO(filters.orderPaymentFormationStartRange)
      : undefined;
    const preparedFilters = {
      humanReadableId: filters.humanReadableId,
      passengerName: filters.passengerFullName ? clearSymbols(filters.passengerFullName).trim() : undefined,
      approverName: filters.approverFullName ? clearSymbols(filters.approverFullName).trim() : undefined,
      startFrom: desiredDateRange ? desiredDateRange.start : undefined,
      startTo: desiredDateRange ? desiredDateRange.end : undefined,
      paymentFormationStart: paymentFormationRange ? paymentFormationRange.start : undefined,
      paymentFormationEnd: paymentFormationRange ? paymentFormationRange.end : undefined,
      costCenter: filters?.costCenter,
      balanceUnitSet: filters?.balanceUnits?.join(',') || undefined,
      organizationId,
    };

    return R.reject(x => typeof x === 'undefined' || x === '')(preparedFilters);
  }

  return { organizationId };
};

export const filter2searchQuery = (filterValues: TaxiYandexRegistryFilter): YandexTaxiReportFilter => {
  const desiredDateRange = filterValues.desiredDateRange ? toDateRangeISO(filterValues.desiredDateRange) : undefined;
  const orderPaymentFormationStartRange = filterValues.orderPaymentFormationStartRange
    ? toDateRangeISO(filterValues.orderPaymentFormationStartRange)
    : undefined;

  const obj = {
    humanReadableId: filterValues.humanReadableId,
    status: filterValues.status?.length ? filterValues.status : undefined,
    startFrom: desiredDateRange ? desiredDateRange.start : undefined,
    startTo: desiredDateRange ? desiredDateRange.end : undefined,
    approverName: filterValues.approverFullName ? clearSymbols(filterValues.approverFullName).trim() : undefined,
    orderPaymentFormationStartRange: orderPaymentFormationStartRange
      ? {
        startDate: orderPaymentFormationStartRange.start,
        endDate: orderPaymentFormationStartRange.end,
      }
      : undefined,
    passengerName: filterValues.passengerFullName ? clearSymbols(filterValues.passengerFullName).trim() : undefined,
    balanceUnits: filterValues.balanceUnits?.length ? filterValues.balanceUnits : undefined,
    costCenter: filterValues?.costCenter ? clearSymbols(filterValues.costCenter).trim() : undefined,
    departments: {
      1: filterValues.department1 && filterValues.department1?.length > 0
        ? filterValues.department1 : [],
      2: filterValues.department2 && filterValues.department2?.length > 0
        ? filterValues.department2 : [],
      3: filterValues.department3 && filterValues.department3?.length > 0
        ? filterValues.department3 : [],
      4: filterValues.department4 && filterValues.department4?.length > 0
        ? filterValues.department4 : [],
      5: filterValues.department5 && filterValues.department5?.length > 0
        ? filterValues.department5 : [],
      6: filterValues.department6 && filterValues.department6?.length > 0
        ? filterValues.department6 : [],
    },
  };

  return R.reject(x => typeof x === 'undefined' || x === '')(obj);
};

export const getKeyValue = <T extends object, U extends keyof T>(key: U) => (obj: T) => obj[key];

export const getFieldName = <T extends object, U extends keyof T>(key: U) => key;

export const capitalize = (str: string): string => str.charAt(0).toUpperCase() + str.slice(1);

export const formatAddress = (
  point?: Waypoint | null
): string => {
  if (!point) {
    return '';
  }
  return [point.city, point.street, point.house].filter(Boolean).join(', ');
};

export const formatRoute = (waypoints: Waypoint[] | undefined) => {
  return waypoints ? waypoints.map(formatAddress).join(' - ') : '-';
};

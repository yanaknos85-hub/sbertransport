import { TablePaginationConfig } from 'antd/lib/table';
import { useState } from 'react';
import {
  PersonalRegistryFilters,
  PersonalSearchQuery,
  SearchedPersonalCoopTrips,
  TripRequestReport
} from 'stores/PersonalSearch/PersonalSearch.interface';
import { useSettingsContext } from 'stores/SettingsContext';
import { b64EncodeUnicode, clearSymbols } from 'utils';
import { toDateRangeISO } from 'shared/components/DateInput/utils';
import * as R from 'ramda';
import { formatTime } from 'utils/formatTime';
import { transformCoopTrip, transformToRangeObject } from 'utils/reportsUtils';
import { fullNameLastFirstPat } from 'utils/employee';
import { RequestBodyPersonalParams, SortSetting, reportParams } from '../types/types';
import { PersonalRegistrySorterResult } from '../hooks/useColumns';
import { DATE_FORMAT } from 'constants/constants.app';
import { prepareSubmitPurposes } from 'utils/purposesOptions';

/**
 * Подготавливает данные для запроса поиска поездок
 * @param data
 * @param sortSetting
 */
export const processSearchSubmitJson = (
  data: PersonalSearchQuery,
  sortSetting?: SortSetting
): PersonalRegistryFilters => {
  const requestBody: PersonalRegistryFilters = {
    ...data,
    requestHumanId: data.requestHumanId ? data.requestHumanId.trim() : undefined,
    coopTrip: transformCoopTrip(data.coopTrip),
    requestStatusSet: data.requestStatusSet?.length ? data.requestStatusSet : undefined,
    economyPercent: transformToRangeObject(data.economyPercent),
    passengerCountSet: data.passengerCountSet?.length ? data.passengerCountSet : undefined,
    tariffIdSet: data.tariffIdSet?.length ? data.tariffIdSet : undefined,
    expectedDistance: transformToRangeObject(data.expectedDistance),
    ratingMarkSet: data.ratingMarkSet?.length ? data.ratingMarkSet : undefined,
    creationDate: data.creationDate ? toDateRangeISO(data.creationDate) : undefined,
    orderPaymentFormationStartRange: data.orderPaymentFormationStartRange
      ? toDateRangeISO(data.orderPaymentFormationStartRange)
      : undefined,
    costCenter: data.costCenter ? clearSymbols(data.costCenter).trim() : undefined,
    employeeFIO: data.employeeFIO ? clearSymbols(data.employeeFIO).trim() : undefined,
    sortSetting,
    pageSetting: data.pageSetting ?? { page: 0, size: 100 },
    purposeSet: data.purposeSet?.length ? prepareSubmitPurposes(data.purposeSet) : undefined,
    desiredDateRange: data.desiredDateRange ? toDateRangeISO(data.desiredDateRange) : undefined,
    savings: data.savings ? data.savings === 'true' ? true : false : undefined,
    requestClosedDatetime: data.requestClosedDatetime ? toDateRangeISO(data.requestClosedDatetime) : undefined,
    department1: data.department1 && data.department1?.length > 0
      ? data.department1 : undefined,
    department2: data.department2 && data.department2?.length > 0
      ? data.department2 : undefined,
    department3: data.department3 && data.department3?.length > 0
      ? data.department3 : undefined,
    department4: data.department4 && data.department4?.length > 0
      ? data.department4 : undefined,
    department5: data.department5 && data.department5?.length > 0
      ? data.department5 : undefined,
    department6: data.department6 && data.department6?.length > 0
      ? data.department6 : undefined,
    employeeOrganizationSet: data.employeeOrganizationSet && data.employeeOrganizationSet.length > 0
      ? data.employeeOrganizationSet : undefined,
  };
  return R.reject(x => typeof x === 'undefined' || x === '')(requestBody) as PersonalRegistryFilters;
};

export const useSavedSortSettings = (): [
  PersonalSearchQuery['sortSetting'],
  (sortSettings: PersonalSearchQuery['sortSetting']) => void
] => {
  const { saveSortSettings, sortSettings: getSortSettings } = useSettingsContext().Personal;

  // TODO: FIX ANY - SortSettings из разных мест
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [sortSettings, setSortSettings] = useState<PersonalSearchQuery['sortSetting']>(getSortSettings as any);

  const saveSettings = (sortSettings: PersonalSearchQuery['sortSetting']) => {
    setSortSettings(sortSettings);
    sortSettings && saveSortSettings && saveSortSettings(sortSettings);
  };

  return [sortSettings, saveSettings];
};

export const getCurrentSharedRideIdArray = (trips: TripRequestReport[]): string[] => {
  const coopTripsWithPassengers = trips.filter(trip => trip.coopTrip);
  const coopTripsIdMap = new Map();
  coopTripsWithPassengers.forEach(item => coopTripsIdMap.set(item.sharedRideId, item));
  const uniqSharedRideIdArray = [] as string[];
  coopTripsIdMap.forEach(
    (item: TripRequestReport) => item.sharedRideId && uniqSharedRideIdArray.push(item.sharedRideId)
  );
  return uniqSharedRideIdArray;
};

export const getCoopTrips = (
  coopTripsResponse: SearchedPersonalCoopTrips,
  sharedRideId: string | null | undefined,
  emptyValue = undefined
): TripRequestReport[] | undefined => (
  sharedRideId ? coopTripsResponse.find(item => item.sharedRideId === sharedRideId)?.trips ?? emptyValue : emptyValue
);

const formatPersonalTime = (time: string | number | null | undefined) => formatTime(time, '-', DATE_FORMAT.DATE_WITH_TIME_DOTS);

export const getTripParam = (trips: TripRequestReport[], emptyValue = '-'): Record<string, string> => {
  const params = {
    costCenter: [],
    desiredDate: [],
    fio: [],
    orderPaymentFormationStartDate: [],
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  } as Record<string, any>;

  const coopTrips = new Map();
  trips.forEach(trip => coopTrips.set(trip.passenger?.id, trip));

  const sortedUniqTrips = Array.from(coopTrips, ([, value]) => value).sort((prev, next) => {
    if (prev.creationTime && next.creationTime) {
      return prev.creationTime - next.creationTime;
    }
    return 0;
  });

  sortedUniqTrips.forEach(trip => {
    params.costCenter.push(trip.costCenter || emptyValue);
    params.desiredDate.push(formatPersonalTime(trip.desiredDate));
    params.fio.push(fullNameLastFirstPat(trip.passenger));
    params.orderPaymentFormationStartDate.push(formatPersonalTime(trip.orderPaymentFormationStartDate));
  });

  for (const key in params) {
    const paramsForString = params[key].map((item: string) => (item === emptyValue ? item : `${item}`));
    params[key] = paramsForString.join('\n');
  }

  return params;
};

export const getTripParams = (
  trip: TripRequestReport | undefined,
  emptyValue = '-'
): Record<string, string> => {
  const costCenter = trip?.costCenter || emptyValue;
  const orderPaymentFormationStartDate = formatPersonalTime(trip?.orderPaymentFormationStartDate);
  const fio = fullNameLastFirstPat(trip?.passenger);
  const desiredDate = formatPersonalTime(trip?.desiredDate);

  return {
    costCenter,
    desiredDate,
    fio,
    orderPaymentFormationStartDate,
  };
};

/**
 * Добавляет к текущей фильтрации параметры сортировки
 * @param filters - объект фильтров
 * @param sorter - необходимые параметры сортировки
 */
export const applySorterToFilters = (filters: PersonalRegistryFilters, sorter: PersonalRegistrySorterResult) => {
  const { sortSetting, ...rest } = filters;

  return sorter.column?.sortProperty
    ? {
      ...filters,
      sortSetting: { property: sorter.column.sortProperty, directionAsc: sorter.order === 'ascend' },
    }
    : !sorter.column
      ? rest
      : filters;
};

/**
 * Добавляет к текущей фильтрации параметры пагинации
 * @param filters - объект фильтров
 * @param pagination - необходимые параметры пагинации
 */
export const applyPaginationToFilters = (
  filters: PersonalRegistryFilters,
  pagination: TablePaginationConfig
): PersonalRegistryFilters => pagination.current && pagination.pageSize
  ? {
    ...filters,
    pageSetting: {
      page: pagination.current - 1,
      size: pagination.pageSize,
    },
  }
  : filters;

/**
 * Возвращает настройки сортировки реестра поездок личного транспорта из sessionStorage
 */
export const getSavedSortSettings = (): PersonalSearchQuery['sortSetting'] | undefined => {
  const stringSettings = sessionStorage.getItem('PersonalRegistrySorting');

  return stringSettings ? JSON.parse(stringSettings) : undefined;
};

export const processRequestParamsOnXlsDownloadPersonal = (
  params: reportParams,
  organizationId: string
): RequestBodyPersonalParams => {
  const {
    filters, columnsVisibility, withFilters, withView, reportType,
  } = params;
  if (filters && withFilters) {
    const preparedFilters = { ...filters } as PersonalRegistryFilters;

    // eslint-disable-next-line no-console
    console.log('personal:', preparedFilters);

    const requestBody: RequestBodyPersonalParams = {
      withFilters,
      withView,
      ...(withView && columnsVisibility && { ...columnsVisibility }),
      filters: b64EncodeUnicode(JSON.stringify(preparedFilters)),
    };

    reportType === 'COMPENSATIONS' && delete requestBody.withView;

    return requestBody;
  }

  return {
    withFilters,
    organizationId,
    filters: b64EncodeUnicode(JSON.stringify({ organizationId })),
    ...(reportType === 'COMPENSATIONS'
      ? {}
      : { withView, ...(withView && columnsVisibility && { ...columnsVisibility }) }),
  };
};

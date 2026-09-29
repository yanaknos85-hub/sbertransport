import { useState } from 'react';
import { useSettingsContext } from 'stores/SettingsContext';
import * as R from 'ramda';

import { reportParams, RequestBodyCarSharing, SortSetting } from '../types';
import { CarSharingReportFilters, CarSharingSearchQuery } from 'stores/CarSharingTrip/CarSharingTrip.interface';
import { b64EncodeUnicode, clearSymbols } from 'utils';
import { transformToRangeObject } from 'utils/reportsUtils';
import { toDateRangeISO } from 'shared/components/DateInput/utils';
import { RangeObjectEnum } from 'modules/TaxiRegistry/types/types';

/**
 * Подготавливает данные для запроса поиска поездок
 * @param data
 */
export const processSearchSubmitJson = (data: CarSharingSearchQuery): CarSharingReportFilters => {
  const requestBody = {
    ...data,
    desiredDate: data.desiredDate ? toDateRangeISO(data.desiredDate) : undefined,
    creationDate: data.creationDate ? toDateRangeISO(data.creationDate) : undefined,
    requestStatusSet: data.requestStatusSet?.length ? data.requestStatusSet : undefined,
    expectedCost: transformToRangeObject(data.expectedCost, RangeObjectEnum.cost),
    passengerCountSet: data.passengerCountSet?.length ? data.passengerCountSet : undefined,
    contractorSet: data.contractorSet?.length ? data.contractorSet : undefined,
    drivingLength: transformToRangeObject(data.drivingLength),
    ratingMarkSet: data.ratingMarkSet?.length ? data.ratingMarkSet : undefined,
    costCenter: data.costCenter ? clearSymbols(data.costCenter).trim() : undefined,
    employeeFIO: data.employeeFIO ? clearSymbols(data.employeeFIO).trim() : undefined,
    personnelNumber: data.personnelNumber ? clearSymbols(data.personnelNumber).trim() : undefined,
    departmentCode: data.departmentCode ?? undefined,
    tariffIdSet: data.tariffIdSet ? [data.tariffIdSet] : undefined,
  } as CarSharingReportFilters;

  return R.reject(x => typeof x === 'undefined' || x === '')(requestBody) as CarSharingReportFilters;
};

export const useSavedSortSettings = (): [
  CarSharingSearchQuery['sortSetting'],
  (sortSettings: CarSharingSearchQuery['sortSetting']) => void,
  () => void
] => {
  const {
    sortSettings: currentSorting, saveSortSettings, deleteSortSettings,
  } = useSettingsContext().CarSharing;
  const [sortSettings, setSortSettings] = useState<CarSharingSearchQuery['sortSetting']>(
    currentSorting() as CarSharingSearchQuery['sortSetting']
  );

  const saveSettings = (sorting: CarSharingSearchQuery['sortSetting']) => {
    setSortSettings(sorting);
    saveSortSettings(sorting as SortSetting);
  };

  return [sortSettings, saveSettings, deleteSortSettings];
};

export const processRequestParamsOnXlsDownloadCarSharing = (
  { filters, withFilters }: reportParams,
  organizationId: string
): RequestBodyCarSharing => {
  if (filters && withFilters) {
    const preparedFilters = { ...filters, organizationId };
    return {
      withFilters,
      filters: b64EncodeUnicode(JSON.stringify(preparedFilters)),
    };
  }

  return {
    withFilters: false,
    filters: b64EncodeUnicode(JSON.stringify({ organizationId })),
  };
};

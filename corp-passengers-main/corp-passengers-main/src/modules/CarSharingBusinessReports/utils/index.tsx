import { useState } from 'react';
import { useSettingsContext } from 'stores/SettingsContext';
import * as R from 'ramda';

import { reportParams, RequestBodyCarSharing, SortSetting } from '../types';
import { CarSharingReportFilters, CarSharingSearchQuery } from 'stores/CarSharingTrip/CarSharingTrip.interface';
import { b64EncodeUnicode, clearSymbols } from 'utils';
import { transformToRangeObject } from 'utils/reportsUtils';
import { toDateRangeISO } from 'shared/components/DateInput/utils';
import { RangeObjectEnum } from 'modules/TaxiRegistry/types/types';
import { prepareSubmitPurposes } from 'utils/purposesOptions';

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
    purposeSet: data.purposeSet?.length ? prepareSubmitPurposes(data.purposeSet) : undefined,
    savings: data.savings ? data.savings === 'true' ? true : false : undefined,
    requestClosedDatetime: data.requestClosedDatetime
      ? toDateRangeISO(data.requestClosedDatetime)
      : undefined,
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
    const preparedFilters = { ...filters };

    // eslint-disable-next-line no-console
    console.log('carsharing:', preparedFilters);

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

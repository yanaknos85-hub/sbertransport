import * as R from 'ramda';
import { PurposeOption, RegisterSearchQuery } from 'api/register-search';
import {
  RangeObjectEnum,
  TaxiRegistryFilter,
  reportParams,
  RequestBodyParamsTaxi
} from 'modules/TaxiRegistry/types/types';
import { b64EncodeUnicode, clearSymbols } from 'utils';
import { UUID } from 'utils/io-ts';
import { transformCoopTrip, transformToRangeObject } from 'utils/reportsUtils';
import { toDateRangeISO } from 'shared/components/DateInput/utils';

export const getPurposeLabelInValue = (
  value: { key?: UUID; label?: string }[] | undefined
): PurposeOption[] => value?.map(el => ({ id: el.key, label: el.label })) ?? [];

export const processTaxiRequestBodyOnXlsDownload = (
  { filters, withFilters }: reportParams,
  organizationId: string,
  isOrganization?: boolean,
  executorGroupId?: string[]
): RequestBodyParamsTaxi => {
  if (filters && withFilters) {
    const preparedFilters = isOrganization
      ? { ...filters, organizationId }
      : { ...filters, executorGroupIds: executorGroupId };

    delete preparedFilters.pageSetting;
    delete preparedFilters.sortSetting;

    const requestBody = {
      withFilters,
      filters: b64EncodeUnicode(JSON.stringify(preparedFilters)),
    };

    // eslint-disable-next-line no-console
    console.log('taxi:', preparedFilters);

    return requestBody;
  }

  const dataForB64 = isOrganization ? organizationId : executorGroupId;
  return { withFilters: false, filters: btoa(JSON.stringify({ dataForB64 })) };
};

export const filter2searchQuery = (filterValues: TaxiRegistryFilter): RegisterSearchQuery => {
  const obj = {
    ...filterValues,
    requestHumanId: filterValues.requestHumanId ? filterValues.requestHumanId.trim() : undefined,
    coopTrip: transformCoopTrip(filterValues.coopTrip),
    requestStatusSet: filterValues.requestStatusSet?.length ? filterValues.requestStatusSet : undefined,
    expectedCost: transformToRangeObject(filterValues.expectedCost, RangeObjectEnum.cost),
    economyPercent: transformToRangeObject(filterValues.economyPercent),
    passengerCountSet: filterValues.passengerCountSet?.length ? filterValues.passengerCountSet : undefined,
    contractorSet: filterValues.contractorSet?.length ? filterValues.contractorSet : undefined,
    tariffIdSet: filterValues.tariffIdSet?.length ? filterValues.tariffIdSet : undefined,
    factDistance: transformToRangeObject(filterValues.factDistance),
    ratingMarkSet: filterValues.ratingMarkSet?.length ? filterValues.ratingMarkSet : undefined,
    creationDate: filterValues.creationDate ? toDateRangeISO(filterValues.creationDate) : undefined,
    desiredDateRange: filterValues.desiredDateRange ? toDateRangeISO(filterValues.desiredDateRange) : undefined,
    costCenter: filterValues.costCenter ? clearSymbols(filterValues.costCenter).trim() : undefined,
    employeeFIO: filterValues.employeeFIO ? clearSymbols(filterValues.employeeFIO).trim() : undefined,
    personnelNumber: filterValues.personnelNumber ? clearSymbols(filterValues.personnelNumber).trim() : undefined,
    departmentCode: filterValues.departmentCode ?? undefined,
    sharedRideOwnerFIO: filterValues.sharedRideOwnerFIO
      ? clearSymbols(filterValues.sharedRideOwnerFIO).trim()
      : undefined,
    pageSetting: filterValues.pageSetting ?? { page: 0, size: 100 },
    department1: filterValues.department1 && filterValues.department1?.length > 0
      ? filterValues.department1 : undefined,
    department2: filterValues.department2 && filterValues.department2?.length > 0
      ? filterValues.department2 : undefined,
    department3: filterValues.department3 && filterValues.department3?.length > 0
      ? filterValues.department3 : undefined,
    department4: filterValues.department4 && filterValues.department4?.length > 0
      ? filterValues.department4 : undefined,
    department5: filterValues.department5 && filterValues.department5?.length > 0
      ? filterValues.department5 : undefined,
    department6: filterValues.department6 && filterValues.department6?.length > 0
      ? filterValues.department6 : undefined,
  };

  return R.reject(x => typeof x === 'undefined' || x === '')(obj);
};

export const getKeyValue = <T extends object, U extends keyof T>(key: U) => (obj: T) => obj[key];

export const getFieldName = <T extends object, U extends keyof T>(key: U) => key;

export const capitalize = (str: string): string => str.charAt(0).toUpperCase() + str.slice(1);

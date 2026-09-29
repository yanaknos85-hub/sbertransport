import * as R from 'ramda';
import {
  RangeObjectEnum,
  GroupTransferRegistryFilter,
  reportParams,
  RequestBodyParamsGroupTransfer
} from 'modules/GroupTransferRegistry/types/types';
import {
  b64EncodeUnicode, clearSymbols, convertToRubles, formatRubles
} from 'utils';
import { transformToRangeObject } from 'utils/reportsUtils';
import { toDateRangeISO } from 'shared/components/DateInput/utils';
import { GroupTransferReportItem, RegistrySearchQuery } from 'stores/GroupTransferRegistry/GroupTransferRegistry';

export const processGroupTransferRequestBodyOnXlsDownload = (
  { filters, withFilters }: reportParams,
  organizationId: string,
  isOrganization?: boolean,
  executorGroupId?: string[]
): RequestBodyParamsGroupTransfer => {
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

    return requestBody;
  }

  const dataForB64 = isOrganization ? organizationId : executorGroupId;
  return { withFilters: false, filters: btoa(JSON.stringify({ dataForB64 })) };
};

export const filter2searchQuery = (filterValues: GroupTransferRegistryFilter): RegistrySearchQuery => {
  const obj = {
    ...filterValues,
    requestHumanId: filterValues.requestHumanId ? filterValues.requestHumanId.trim() : undefined,
    requestStatusSet: filterValues.requestStatusSet?.length ? filterValues.requestStatusSet : undefined,
    expectedCost: transformToRangeObject(filterValues.expectedCost, RangeObjectEnum.cost),
    contractorSet: filterValues.contractorSet?.length ? filterValues.contractorSet : undefined,
    tariffIdSet: filterValues.tariffIdSet?.length ? filterValues.tariffIdSet : undefined,
    factDistance: transformToRangeObject(filterValues.factDistance),
    expectedDistance: transformToRangeObject(filterValues.factDistance),
    ratingMarkSet: filterValues.ratingMarkSet?.length ? filterValues.ratingMarkSet : undefined,
    creationDate: filterValues.creationDate ? toDateRangeISO(filterValues.creationDate) : undefined,
    desiredDateRange: filterValues.desiredDateRange ? toDateRangeISO(filterValues.desiredDateRange) : undefined,
    costCenter: filterValues.costCenter ? clearSymbols(filterValues.costCenter).trim() : undefined,
    employeeFIO: filterValues.employeeFIO ? clearSymbols(filterValues.employeeFIO).trim() : undefined,
    personnelNumber: filterValues.personnelNumber ? clearSymbols(filterValues.personnelNumber).trim() : undefined,
    departmentCode: filterValues.departmentCode ?? undefined,
    pageSetting: filterValues.pageSetting ?? { page: 0, size: 100 },
    groupTransferClassList:
      filterValues.groupTransferClassList?.length
        ? filterValues.groupTransferClassList
        : undefined,
    purposeSet:
      filterValues.purposeSet?.length
        ? filterValues.purposeSet.map(({ label: purpose, value: id }) => ({ id, purpose }))
        : undefined,
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

export const capitalize = (str: string): string => str.charAt(0).toUpperCase() + str.slice(1);

export const processJournalData = (data: GroupTransferReportItem[]): GroupTransferReportItem[] => data.map(item => {
  return {
    ...item, expected: {
      ...item.expected, cost: item.expected?.cost
      && formatRubles(convertToRubles(item.expected.cost)) as unknown as number,
    },
    authorFIO: item.author.fio,
    expectedCost: item.expected?.cost,
    contractorName: item.contractor?.name,
    tripFactPrice: item.factData?.tripFactPrice,
    expectedDistance: item.expected?.distance,
    tripFactDistance: item.factData?.tripFactDistance,
    driverPhone: item.driver?.contactPhone,
  };
});


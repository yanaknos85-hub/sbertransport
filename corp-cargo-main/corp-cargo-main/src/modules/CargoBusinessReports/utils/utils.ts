import { CargoRegistryFilters, SortSettings } from 'stores/CargoRegistry/CargoRegistry.interface';
import { TablePaginationConfig } from 'antd/lib/table';
import * as R from 'ramda';
import { toDateRangeISO } from 'shared/components/DateInput/utils';
import { b64EncodeUnicode, clearSymbols } from 'utils';
import {
  Filters, reportParams, RequestBodyPublic, RequestBodyPublicParams
} from '../types/types';

/**
 * Подготавливает данные для запроса поиска поездок
 * @param data
 * @param sortSetting
 */
export const processCreateSubmitJson = (data: Filters, sortSettings?: SortSettings): CargoRegistryFilters => {
  const { pageSetting, ...restData } = data;

  const requestBody: CargoRegistryFilters = {
    ...restData,
    requestHumanId: data.requestHumanId ? data.requestHumanId.trim() : undefined,
    transportType: data.transportType?.length ? data.transportType : undefined,
    contractorSet: data.contractorSet?.length ? data.contractorSet : undefined,
    authorFIO: data.authorFIO ? clearSymbols(data.authorFIO) : undefined,
    requestStatusSet: data.requestStatusSet?.length ? data.requestStatusSet : undefined,
    deadlineDate: data.deadlineDate ? data.deadlineDate : undefined,
    desiredDate: data.desiredDate ? toDateRangeISO(data.desiredDate) : undefined,
    creationDate: data.creationDate ? toDateRangeISO(data.creationDate) : undefined,
    changeDate: data.changeDate ? toDateRangeISO(data.changeDate) : undefined,
    sortSetting: sortSettings,
    organizationSet: data.organizationSet?.length ? data.organizationSet : undefined,
    department1: data.department1?.length ? data.department1 : undefined,
    department2: data.department2?.length ? data.department2 : undefined,
    department3: data.department3?.length ? data.department3 : undefined,
    department4: data.department4?.length ? data.department4 : undefined,
    department5: data.department5?.length ? data.department5 : undefined,
    department6: data.department6?.length ? data.department6 : undefined,
  };

  return R.reject(x => typeof x === 'undefined' || x === '')(requestBody) as CargoRegistryFilters;
};

// export const processJournalData = (data: TripInfoForReporting[], tripStatus: TripStatus[]): JournalRecord[] => [
//   ...data.map((res: TripInfoForReporting) => ({
//     id: res.id,
//     requestIdVisible: res.humanReadableId ?? '-',
//     mvzVisible: res.costCenter ?? '-',
//     desiredDateVisible: formatTime(res.desiredDate, '-', DATE_FORMAT.DATE_WITH_TIME_DOTS),
//     controlPeriodOfPayment: '-',
//     approveDateVisible: formatTime(res.approveDate, '-', DATE_FORMAT.DATE_WITH_TIME_DOTS),
//     passengerFioVisible: res.passenger ? fullName(res.passenger) : '-',
//     requestStatusVisible: tripStatus.find(status => status.name === res.status)?.rusName ?? '-',
//     plannedPriceVisible: formatRubles(convertToRubles(res.expected.cost)).replace(RUBLE_SIGN, ''),
//     expectedDistanceVisible: formatDistance(res.expected.distance),
//     paymentPeriodVisible: res.paymentQuarter ?? '-',
//     departmentCodeVisible: res.department.code ?? '-',
//   })),
// ];

/**
 * Добавляет к текущей фильтрации параметры сортировки
 * @param filters - объект фильтров
 * @param sorter - необходимые параметры сортировки
 */
// export const applySorterToFilters = (
//   filters: PublicRegistryFilters,
//   sorter?: TripRegistrySorterResult
// ): PublicRegistryFilters => {
//   if (sorter.column?.sortProperty) {
//     filters.sortSetting = {
//       property: sorter.column.sortProperty,
//       directionAsc: sorter.order === 'ascend',
//     };
//   }
//   if (!sorter.column) {
//     delete filters.sortSetting;
//   }
//   return filters;
// };

/**
 * Добавляет к текущей фильтрации параметры пагинации
 * @param filters - объект фильтров
 * @param pagination - необходимые параметры пагинации
 */
export const applyPaginationToFilters = (
  filters: CargoRegistryFilters,
  pagination: TablePaginationConfig
): CargoRegistryFilters => {
  if (pagination.current && pagination.pageSize) {
    filters.pageSetting = {
      page: pagination.current - 1,
      size: pagination.pageSize,
    };
  }
  return filters;
};

/**
 * Формирует requestBody для запросов выгрузки отчетов xlsx для реестров
 * @param params - параметры выгрузки
 */
export const processRequestParamsOnXlsDownloadPublic = (
  params: reportParams,
  organizationId: string
): RequestBodyPublic => {
  const {
    filters, columnsVisibility, withFilters, withView, reportType,
  } = params;
  if (filters && withFilters) {
    const preparedFilters = {
      ...filters,
    } as Partial<RequestBodyPublicParams>;

    // eslint-disable-next-line no-console
    const requestBody = {
      withFilters,
      withView,
      ...(withView && columnsVisibility && { ...columnsVisibility }),
      filters: b64EncodeUnicode(JSON.stringify(preparedFilters)),
    } as RequestBodyPublic;

    reportType === 'COMPENSATIONS' && delete requestBody.withView;

    return requestBody;
  }

  return reportType === 'COMPENSATIONS'
    ? { withFilters, organizationId }
    : {
      withFilters,
      organizationId,
      withView,
      ...(withView && columnsVisibility && { ...columnsVisibility }),
    };
};

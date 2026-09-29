import {
  PublicRegistryFilters,
  SortSettings,
  TripInfoForReporting
} from 'stores/PublicRegistry/models/PublicRegistry.interface';
import { TripStatus } from 'api/travel-status';
import { TablePaginationConfig } from 'antd/lib/table';
import * as R from 'ramda';
import { toDateRangeISO } from 'shared/components/DateInput/utils';
import {
  b64EncodeUnicode, clearSymbols, formatRubles, formatTime
} from 'utils';
import { fullName } from 'utils/employee';
import { prepareSubmitPurposes } from 'utils/purposesOptions';
import { DATE_FORMAT, RUBLE_SIGN } from 'constants/constants.app';
import {
  Filters, JournalRecord, reportParams, RequestBodyPublic, RequestBodyPublicParams
} from '../types/types';
import { TripRegistrySorterResult } from '../hooks/useColumns';
import { formatDistance } from 'utils/formatDistance';
import { TripPurpose } from 'stores/TripPurposes/TripPurpose.interface';

/**
 * Подготавливает данные для запроса поиска поездок
 * @param data
 * @param sortSetting
 */
export const processSearchSubmitJson = (data: Filters, sortSetting?: SortSettings): PublicRegistryFilters => {
  const requestBody: PublicRegistryFilters = {
    ...data,
    requestHumanId: data.requestHumanId ? data.requestHumanId.trim() : undefined,
    tariffIdSet: data.tariffIdSet?.length ? data.tariffIdSet : undefined,
    requestStatusSet: data.requestStatusSet?.length ? data.requestStatusSet : undefined,
    compensationType: data.compensationType?.length ? data.compensationType : undefined,
    purposeSet: data.purposeSet?.length ? prepareSubmitPurposes(data.purposeSet) : undefined,
    ratingMarkSet: data.ratingMarkSet?.length ? data.ratingMarkSet : undefined,
    creationDate: data.creationDate ? toDateRangeISO(data.creationDate) : undefined,
    orderPaymentFormationStartDate: data.orderPaymentFormationStartDate
      ? toDateRangeISO(data.orderPaymentFormationStartDate)
      : undefined,
    costCenter: data.costCenter ? clearSymbols(data.costCenter).trim() : undefined,
    employeeFIO: data.employeeFIO && clearSymbols(data.employeeFIO),
    sortSetting,
    pageSetting: data.pageSetting ?? { page: 0, size: 100 },
  };

  return R.reject(x => typeof x === 'undefined' || x === '')(requestBody) as PublicRegistryFilters;
};

export const processJournalData = (data: TripInfoForReporting[], tripStatus: TripStatus[]): JournalRecord[] => [
  ...data.map((res: TripInfoForReporting) => ({
    id: res.id,
    requestIdVisible: res.humanReadableId ?? '-',
    mvzVisible: res.costCenter ?? '-',
    desiredDateVisible: formatTime(res.desiredDate, '-', DATE_FORMAT.DATE_WITH_TIME_DOTS),
    controlPeriodOfPayment: '-',
    approveDateVisible: formatTime(res.approveDate, '-', DATE_FORMAT.DATE_WITH_TIME_DOTS),
    passengerFioVisible: res.passenger ? fullName(res.passenger) : '-',
    requestStatusVisible: tripStatus.find(status => status.name === res.status)?.rusName ?? '-',
    plannedPriceVisible: formatRubles(res.expected.cost).replace(RUBLE_SIGN, ''),
    expectedDistanceVisible: formatDistance(res.expected.distance),
    paymentPeriodVisible: res.paymentQuarter ?? '-',
    departmentCodeVisible: res.department.code ?? '-',
  })),
];

/**
 * Добавляет к текущей фильтрации параметры сортировки
 * @param filters - объект фильтров
 * @param sorter - необходимые параметры сортировки
 */
export const applySorterToFilters = (
  filters: PublicRegistryFilters,
  sorter: TripRegistrySorterResult
): PublicRegistryFilters => {
  if (sorter.column?.sortProperty) {
    filters.sortSetting = {
      property: sorter.column.sortProperty,
      directionAsc: sorter.order === 'ascend',
    };
  }
  if (!sorter.column) {
    delete filters.sortSetting;
  }
  return filters;
};

/**
 * Добавляет к текущей фильтрации параметры пагинации
 * @param filters - объект фильтров
 * @param pagination - необходимые параметры пагинации
 */
export const applyPaginationToFilters = (
  filters: PublicRegistryFilters,
  pagination: TablePaginationConfig
): PublicRegistryFilters => {
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
  organizationId: string,
  purposes: TripPurpose[]
): RequestBodyPublic => {
  const {
    filters, columnsVisibility, withFilters, withView, reportType,
  } = params;
  if (filters && withFilters) {
    const preparedFilters = {
      ...filters,
      organizationId,
    } as Partial<RequestBodyPublicParams>;

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

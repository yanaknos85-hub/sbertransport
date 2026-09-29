import { useMemo } from 'react';
import { formatTime } from 'utils/formatTime';
import { formatPassengerName } from 'utils/formatPassengerName';
import { DATE_FORMAT } from 'constants/constants.app';

import {
  YandexTaxiReportResponse
} from 'api/yandexTaxiRegistry/yandex-taxi-registry.types';
import {
  YandexTaxiRequestStatus,
  YandexTaxiRequestStatusTitles,
  YandexTaxiTariffTitles
} from 'api/yandexTaxiRegistry/yandex-taxi-registry.constants';

import { TableRecord } from '../types/types';
import { formatRoute } from '../utils/data';

const formatTimeString = (time: string | number | null | undefined) => formatTime(time, '-', DATE_FORMAT.DATE_WITH_TIME_DOTS);

export const useTransformedData = (data: YandexTaxiReportResponse | null): TableRecord[] => useMemo(() => {
  const transformedData = data?.content.map(request => {
    const status = request.status as YandexTaxiRequestStatus;

    return {
      id: request.id as string,
      humanReadableId: request.humanReadableId || '-',
      passengerFullName: formatPassengerName(request.passenger),
      approverFullName: formatPassengerName(request.approver),
      desiredDate: formatTimeString(request.tripDate),
      // requestCompletionDate: '-', // есть в БТ, пока не приходит в ответе с бэка
      tariff: YandexTaxiTariffTitles[request.tariff],
      requestStatus: YandexTaxiRequestStatusTitles[status],
      // approvalDate: '-', // есть в БТ, пока не приходит в ответе с бэка
      plannedRoute: formatRoute(request.waypoints),
      // actualRoute: '-', // есть в БТ, пока не приходит в ответе с бэка
      plannedCost: request.plannedCost || 0,
      actualCost: request.factCost || 0,
      // actualDistance: 0, // есть в БТ, пока не приходит в ответе с бэка
      receiptLink: request.receipt || '-',
      comment: request.comment || '-',
      reason: request.reason || '-',
      costCenter: request.costCenter || '-',
    };
  }
  ) || [];

  return transformedData;
},
[data]
);

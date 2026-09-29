import type { MutationResultPair } from 'react-query';
import type { AxiosError } from 'axios';

import { useAPI, useAPIMutation } from 'api';
import type { APIQueryResult } from 'api';
import type { UUID } from 'utils/io-ts';
import { ignore } from 'utils/Misc';
import {
  YandexTaxiReportItem,
  YandexTaxiReportResponse,
  YandexTaxiReportSearchQuery,
  YandexTaxiUpdateStatusesParams
} from './yandex-taxi-registry.types';
import { GET_YANDEX_TAXI_REGISTRY_ITEM, GET_YANDEX_TAXI_REGISTRY_LIST } from './yandex-taxi-registry.constants';

declare module 'api' {
  interface Cache {
    report: { key: ['yandexTaxiReport', YandexTaxiReportSearchQuery]; value: YandexTaxiReportResponse };
    request: { key: ['request', UUID]; value: YandexTaxiReportItem };
  }
}

export const useYandexTaxiRegistry = (query: YandexTaxiReportSearchQuery): APIQueryResult<
  YandexTaxiReportResponse,
  Error
> => (
  useAPI(['yandexTaxiReport', query], ({ http, process }) => (
    http
      .post<YandexTaxiReportResponse>(GET_YANDEX_TAXI_REGISTRY_LIST, query)
      .then(process.decodeResponseData(YandexTaxiReportResponse))
  ))
);

export const useUpdateExternalStatuses = (): MutationResultPair<
  unknown,
  AxiosError,
  YandexTaxiUpdateStatusesParams,
  unknown
> => (
  useAPIMutation(({ http }, { status, requestId }) => (
    http.patch<unknown>(`/request/external/${requestId}`, [{
      op: 'REPLACE',
      path: '/status',
      value: status,
    }])
  ), { onSuccess: ignore })
);

export const useYandexTaxiRegistryById = (id: UUID): APIQueryResult<YandexTaxiReportItem, Error> => (
  useAPI(['request', id], ({ http, process }) => (
    http
      .get<YandexTaxiReportItem>(GET_YANDEX_TAXI_REGISTRY_ITEM, { urlParams: { id } })
      .then(process.decodeResponseData(YandexTaxiReportItem))
  ))
);


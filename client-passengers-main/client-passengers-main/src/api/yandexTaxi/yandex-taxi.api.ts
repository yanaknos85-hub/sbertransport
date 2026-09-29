import { useAPI, useAPIMutation } from 'api';
import type { APIQueryResult } from 'api';
import { UUID } from 'utils/io-ts';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores';

import {
  YandexTaxiRequest,
  YandexTaxiRequestResponse,
  YandexTaxiRequestSearchQuery,
  YandexTaxiUpdateQuery,
  YandexTaxiLimitAllowance,
  YandexTariffResponseType
} from './yandex-taxi.types';
import {
  YANDEX_TAXI_ORDER,
  GET_YANDEX_TAXI_ORDERS_LIST,
  RequestType,
  ResponseFormat,
  GET_YANDEX_TAXI_ALLOWANCE,
  GET_YANDEX_TAXI_TARIFFS
} from './yandex-taxi.constants';
import { MutationResultPair, QueryConfig } from 'react-query';
import { SYSTEM_MESSAGES } from 'constants/constants.app';
import { AxiosError } from 'axios';
import QueryString from 'qs';

declare module 'api' {
  interface Cache {
    requestList: { key: ['yandexTaxiRequestList']; value: YandexTaxiRequestResponse };
    request: { key: ['request', UUID]; value: YandexTaxiRequest };
    limitAllowance: { key: ['yandexLimit']; value: YandexTaxiLimitAllowance };
    yandexTariffs: { key: ['yandexTariffs', string]; value: YandexTariffResponseType };
    yandexReceipt: { key: ['yandexReceipt', string]; value: unknown };
    yandexTaxiTripsList: { key: ['yandexTaxiTripsList', YandexTaxiRequestSearchQuery]; value: YandexTaxiRequestResponse };
  }
}

export const useYandexTaxiLimitAllowance = (config?: QueryConfig<YandexTaxiLimitAllowance>) => {
  return useAPI(['yandexLimit'], ({ http, process }) => http
    .get<YandexTaxiLimitAllowance>(GET_YANDEX_TAXI_ALLOWANCE)
    .then(process.decodeResponseData(YandexTaxiLimitAllowance)), config);
};

export const useYandexTaxiTariffs = (coordinates: string, config?: QueryConfig<YandexTariffResponseType>) => {
  return useAPI(['yandexTariffs', coordinates], ({ http, process }) => http.get<YandexTariffResponseType>(GET_YANDEX_TAXI_TARIFFS, {
    params: {
      coordinates,
    },
  }).then(process.decodeResponseData(YandexTariffResponseType))
  , config);
};

export const useYandexTaxiTripsList
  = ({
    query, kind, format, passengerId, approverId,
  }: { query: YandexTaxiRequestSearchQuery;
    kind: RequestType;
    format: ResponseFormat;
    passengerId?: string;
    approverId?: string;
  }
  ): APIQueryResult<YandexTaxiRequestResponse, Error> => {
    return (
      useAPI(
        ['yandexTaxiTripsList', query],
        ({ http, process }) => http
          .get<YandexTaxiRequestResponse>(
            GET_YANDEX_TAXI_ORDERS_LIST,
            {
              params: {
                format,
                approver: approverId,
                passenger: passengerId,
                humanReadableId: query.humanReadableId,
                kind,
                status: query.requestStatusSet,
                passengerName: query.passengerFullName,
                approverName: query.approverFullName,
                startFrom: query.desiredDateRange?.start,
                startTo: query.desiredDateRange?.end,
                page: query.pageSetting?.page || 0,
                size: query.pageSetting?.size || 30,
                sort: query.sortSetting?.property,
                direction: query.sortSetting?.directionAsc ? 'ASC' : 'DESC',
              },
              paramsSerializer: params1 => QueryString.stringify(params1, { arrayFormat: 'repeat' }),
            })
          .then(process.decodeResponseData(YandexTaxiRequestResponse))
      )
    );
  };

export const useYandexTaxiRequestList
  = ({
    query, kind, format, passengerId, approverId,
  }: { query: YandexTaxiRequestSearchQuery;
    kind: RequestType;
    format: ResponseFormat;
    passengerId?: string;
    approverId?: string;
  }
  ): APIQueryResult<YandexTaxiRequestResponse, Error> => {
    return (
      useAPI(
        ['yandexTaxiRequestList'],
        ({ http, process }) => http
          .get<YandexTaxiRequestResponse>(
            GET_YANDEX_TAXI_ORDERS_LIST,
            {
              params: {
                kind,
                format,
                approver: approverId,
                passenger: passengerId,
                humanReadableId: query.humanReadableId,
                status: query.requestStatusSet?.join(','),
                passengerName: query.passengerFullName,
                approverName: query.approverFullName,
                startFrom: query.desiredDateRange?.start,
                startTo: query.desiredDateRange?.end,
                page: query.pageSetting?.page || 0,
                size: query.pageSetting?.size || 30,
                sort: query.sortSetting?.property,
                direction: query.sortSetting?.directionAsc ? 'ASC' : 'DESC',
              },
            })
          .then(process.decodeResponseData(YandexTaxiRequestResponse))
      )
    );
  };

export const useYandexTaxiApprovals = (query: YandexTaxiRequestSearchQuery) => {
  const {
    [StoreNames.selfStore]: selfStore,
  } = useAppStoreContext();
  const { selfEmployee } = selfStore;
  return useYandexTaxiRequestList({
    query,
    kind: RequestType.APPROVAL,
    format: ResponseFormat.FULL,
    approverId: selfEmployee.id,
  });
};

export const useYandexTaxiTrips = (query: YandexTaxiRequestSearchQuery) => {
  const {
    [StoreNames.selfStore]: selfStore,
  } = useAppStoreContext();
  const { selfEmployee } = selfStore;
  return useYandexTaxiRequestList({
    query,
    kind: RequestType.PASSENGER,
    format: ResponseFormat.FULL,
    passengerId: selfEmployee.id,
  });
};

export const useYandexTaxiTripsQuery = (query: YandexTaxiRequestSearchQuery) => {
  const { [StoreNames.selfStore]: selfStore } = useAppStoreContext();
  const { selfEmployee } = selfStore;
  return useYandexTaxiTripsList({
    query, kind: RequestType.PASSENGER, format: ResponseFormat.LIST, passengerId: selfEmployee.id,
  });
};

export const useYandexTripFieldUpdate = (): MutationResultPair<unknown, AxiosError, { id: string; fields: { value: unknown; path: string }[] }, unknown> => useAPIMutation(({ http, process }, {
  id, fields,
}) => http.patch(YANDEX_TAXI_ORDER, [...fields.map(field => ({
  ...field,
  op: 'REPLACE',
}))], {
  urlParams: {
    id,
  },
}).then(process.getResponseData), {
  onSuccess: ({ process, cache }) => {
    process.processStatus(200, SYSTEM_MESSAGES.statusUpdated);
    cache.invalidateQueries(['yandexTaxiTripsList']);
    cache.refetchQueries(['yandexTaxiTripsList']);
  },
  onError: ({ logger }) => {
    logger.toMessage('error', SYSTEM_MESSAGES.errorOccurred);
  },
});

export const useGetYandexReceipt = (url: string, config?: QueryConfig<unknown>) => useAPI(['yandexReceipt', url], ({ http, process }) => {
  return http.get(url).then(process.getResponseData);
}, config);

export const useYandexTaxiRequestById = (id: UUID): APIQueryResult<YandexTaxiRequest, Error> => (
  useAPI(
    ['request', id],
    ({ http, process }) => http
      .get<YandexTaxiRequest>(
        YANDEX_TAXI_ORDER,
        {
          urlParams: { id },
          params: {
            format: ResponseFormat.FULL,
          },
        })
      .then(process.decodeResponseData(YandexTaxiRequest))
  )
);

export const useYandexTaxiUpdateStatus = (): MutationResultPair<unknown, AxiosError, { id: string;query: YandexTaxiUpdateQuery }, unknown> => {
  return useAPIMutation(
    ({ http, process }, { id, query }) => (
      http.put(YANDEX_TAXI_ORDER, query, { urlParams: { id } })
        .then(process.getResponseData)
    ), {
      onSuccess: ({ process, cache }) => {
        process.processStatus(200, SYSTEM_MESSAGES.statusUpdated);
        cache.invalidateQueries(['yandexTaxiRequestList']);
      },
      onError: ({ logger }) => {
        logger.toMessage('error', SYSTEM_MESSAGES.approveFailure);
      },
    }
  );
};

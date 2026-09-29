import { useAPI } from 'api';
import type { APIQueryResult } from 'api';
import { StoreNames, useAppStore } from 'stores';
import type { QueryConfig } from 'react-query';

import {
  YandexTaxiRequestResponse
} from './yandex-taxi.types';
import {
  GET_YANDEX_TAXI_ORDERS_LIST,
  RequestType,
  ResponseFormat,
  YandexTaxiRequestStatus
} from './yandex-taxi.constants';
import { ignore } from 'utils';

declare module 'api' {
  interface Cache {
    yandexTaxiApprovals: { key: ['yandexTaxiApprovals']; value: YandexTaxiRequestResponse };
  }
}

// пока нет отдельной ручки получения количества заявок, ожидающих согласования - используем костыль:
// делаем запрос списка заявок с фильтром по статусу и количеством - 0 заявок.
// в ответе в пагинации атрибут total - общее колво заявок , отвечающих фильтру по статусу, это значение и берем для индикатора в меню
// TODO потом заменить на отдельный запрос, по готовности бэка
export const useYandexTaxiApprovalsList = (
  config?: QueryConfig<YandexTaxiRequestResponse>
): APIQueryResult<YandexTaxiRequestResponse, Error> => {
  const {
    [StoreNames.selfStore]: selfStore,
  } = useAppStore();
  const { selfEmployee } = selfStore;

  return (
    useAPI(
      ['yandexTaxiApprovals'],
      ({ http, process }) => http
        .get<YandexTaxiRequestResponse>(
          GET_YANDEX_TAXI_ORDERS_LIST,
          {
            params: {
              kind: RequestType.APPROVAL,
              format: ResponseFormat.LIST,
              status: YandexTaxiRequestStatus.CONFIRMATION,
              approver: selfEmployee.id,
              page: 0,
              size: 0,
            },
          }
        )
        .then(process.decodeResponseData(YandexTaxiRequestResponse))
        .catch(ignore as any),
      config
    )
  );
};

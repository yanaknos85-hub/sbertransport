import { APIQueryResult, useAPI, useAPIMutation } from 'api';

import { EXCHANGE_FILTERS_SAVE } from '../api/constants';
import { KEYS } from './types';

export interface FilterValues {
  addressFrom?: string;
  addressTo?: string;
}

export interface FilterResponse {
  addressFrom?: string;
  addressTo?: string;
}

declare module 'api' {
  interface Cache {
    exchangeFilters: {
      key: [KEYS.EXCHANGE_FILTERS];
      value: FilterResponse | null;
    };
  }
}

export const useGetFilters = (): APIQueryResult<FilterResponse | null, unknown> => {
  const result = useAPI(
    [KEYS.EXCHANGE_FILTERS],
    ({ http, process }) => {
      return http
        .get<FilterResponse>(EXCHANGE_FILTERS_SAVE)
        .then(process.getResponseData)
        .catch(err => {
          process.processStatus(404, err);
          return null;
        });
    }
  );

  return result;
};

export const useSaveFilters = () => useAPIMutation(
  ({ http, process }, { addressFrom, addressTo }: FilterValues) => {
    return http
      .post<FilterResponse>(EXCHANGE_FILTERS_SAVE, { addressFrom, addressTo })
      .then(process.getResponseData);
  },
  {
    onSuccess: async ({
      result, process, cache,
    }) => {
      process.processStatus(200, 'Фильтр сохранен');
      // result содержит сохранённые данные, их можно использовать для обновления формы
      // Обновляем кэш напрямую, чтобы не делать повторный запрос
      // @ts-ignore
      cache.setQueryData([KEYS.EXCHANGE_FILTERS], result);
    },
    onError: ({ logger }) => {
      logger.toMessage('error', 'Не удалось сохранить фильтр');
    },
  }
);

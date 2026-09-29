import { MutationResultPair, QueryConfig } from 'react-query';

import { APIQueryResult, useAPI, useAPIMutation } from 'api';
import { SYSTEM_MESSAGES } from 'constants/constants.app';

import { DetailedViewExchange, Evaluation } from '../types';
import { ExchangeResponse } from '../types';
import {
  EXCHANGE_ACCEPT,
  EXCHANGE_DENY,
  EXCHANGE_DETAILED,
  EXCHANGE_EVALUATION,
  EXCHANGE_HISTORY
} from './constants';
import { CargoHistory, KEYS } from './types';

declare module 'api' {
  interface Cache {
    exchangeDetailed: {
      key: [KEYS.EXCHANGE_DETAILED, string];
      value: DetailedViewExchange;
    };
    history: {
      key: [KEYS.HISTORY, string];
      value: CargoHistory[];
    };
    evaluation: {
      key: [KEYS.EVALUATION, string];
      value: Evaluation | {};
    };
  }
}

export const useGetDetailed = (
  id: string
): APIQueryResult<DetailedViewExchange, unknown> => useAPI(
  [KEYS.EXCHANGE_DETAILED, id],
  ({ http, process }) => http
    .get<DetailedViewExchange>(EXCHANGE_DETAILED, { urlParams: { id } })
    .then(process.getResponseData)
);

export const useDeny = (): MutationResultPair<unknown, unknown, { id: string }, unknown> => useAPIMutation(
  ({ http, process }, { id }) => http
    .put(EXCHANGE_DENY, {}, {
      urlParams: {
        id,
      },
    })
    .then(process.getResponseData),
  {
    onSuccess: ({ process }) => {
      process.processStatus(200, SYSTEM_MESSAGES.exchangeDeclineSuccess);
    },
    onError: ({ logger }) => logger.toMessage('error', 'Статус не обновлён'),
  }
);

export const useTakeToWork = (): MutationResultPair<ExchangeResponse, unknown, { id: string }, unknown> => useAPIMutation(
  ({ http, process }, { id }) => http
    .put<ExchangeResponse>(EXCHANGE_ACCEPT, {}, { urlParams: { id } })
    .then(process.getResponseData),
  {
    onSuccess: ({
      process,
    }) => {
      process.processStatus(200, SYSTEM_MESSAGES.exchangeTakeToWorkSuccess);
    },
  }
);

export const useGetHistory = (
  rqUuid: string
): APIQueryResult<CargoHistory[], unknown> => useAPI(
  [KEYS.HISTORY, rqUuid],
  ({ http, process }) => http
    .get<CargoHistory[]>(EXCHANGE_HISTORY, { urlParams: { rqUuid } })
    .then(process.getResponseData)
);

export const useEvaluation = (
  rqUuid: string,
  config: QueryConfig<Evaluation | {}, unknown>
): APIQueryResult<Evaluation | {}, unknown> => useAPI(
  [KEYS.EVALUATION, rqUuid],
  ({ http, process }) => http
    .get<Evaluation | {}>(EXCHANGE_EVALUATION, { urlParams: { rqUuid } })
    .then(process.getResponseData)
    .catch(err => {
      process.processStatus(err.response.status, err);
      return null;
    }),
  config
);

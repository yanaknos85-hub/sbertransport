import type {
  MutationResultPair,
  QueryConfig
} from 'react-query';
import type { AxiosError } from 'axios';

import { useAPI, useAPIMutation, APIQueryResult } from 'api';
import { getErrorMessage, ignore } from 'utils/utils';
import {
  SHIFTS_LIST,
  SHIFTS_MASS_CREATE_FIRST_TITLE,
  SHIFTS_SEND_TITLE,
  EWB_WEBSOCKET
} from './shifts.constants';
import {
  Shifts,
  ShiftsFilters,
  TShiftsResponse,
  TMassCreateFirstTitleRequest,
  TMassCreateFirstTitleResponse,
  TSendFirstTitleBatchRequest,
  TSendFirstTitleResponse,
  TEwbShiftResponse,
  EwbShiftResponse
} from './shifts.types';
import { useWebsocket } from 'api/websocket';

export const SHIFTS_KEY = 'shifts';

declare module 'api' {
  interface Cache {
    shifts: {
      key: [typeof SHIFTS_KEY, ShiftsFilters];
      value: TShiftsResponse;
    };
  }
}

export const useShifts = (
  filters: ShiftsFilters,
  config?: QueryConfig<TShiftsResponse>
): APIQueryResult<TShiftsResponse> => (
  useAPI(
    [SHIFTS_KEY, filters],
    ({ http, process }) => (
      http
        .get<TShiftsResponse>(SHIFTS_LIST, { params: filters })
        .then(process.decodeResponseData(Shifts))
    ),
    {
      keepPreviousData: true,
      ...config,
    }
  )
);

export const useMassCreateFirstTitle = (): MutationResultPair<
  TMassCreateFirstTitleResponse,
  AxiosError<Error>,
  TMassCreateFirstTitleRequest,
  unknown
> => (
  useAPIMutation(({ http, process }, data) => (
    http
      .post<TMassCreateFirstTitleResponse>(SHIFTS_MASS_CREATE_FIRST_TITLE, data)
      .then<TMassCreateFirstTitleResponse>(process.getResponseData)
  ), {
    onSuccess: ignore,
    onError: ({ error, logger }) => {
      let description = error.message;
      if (error.response?.data) {
        const { data } = error.response;
        if (data.message) description = data.message;
        else if (typeof data === 'string') description = data;
      }
      logger.toNotify('error', description, getErrorMessage(error));
    },
  })
);

export const useSendFirstTitle = (): MutationResultPair<
  TSendFirstTitleResponse,
  AxiosError<Error>,
  TSendFirstTitleBatchRequest,
  unknown
> => (
  useAPIMutation(({ http, process }, data) => (
    http
      .post<TSendFirstTitleResponse>(SHIFTS_SEND_TITLE, data)
      .then<TSendFirstTitleResponse>(process.getResponseData)
  ), {
    onSuccess: ({ logger, t }) => {
      const { api: i18n } = t.Shifts.sendTitle;
      logger.toMessage('success', i18n.success.description);
    },
    onError: ({ error, logger }) => {
      let description = error.message;
      if (error.response?.data) {
        const { data } = error.response;
        if (data.message) description = data.message;
        else if (typeof data === 'string') description = data;
      }
      logger.toNotify('error', description, getErrorMessage(error));
    },
  })
);

export const useEwbWebsocket = () => useWebsocket<TEwbShiftResponse>(EWB_WEBSOCKET, EwbShiftResponse);

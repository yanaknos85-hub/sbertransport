import type { MutationResultPair, QueryConfig } from 'react-query';
import type { AxiosError } from 'axios';

import { useAPI, useAPIMutation } from 'api';
import { getErrorMessage, ignore } from 'utils/utils';
import { UUID } from 'utils/io-ts';
import {
  WAYBILL_CANCEL,
  WAYBILL_CLOSE,
  WAYBILL_FIRST_TITLE, WAYBILL_ONE, WAYBILL_SEARCH, WAYBILL_TITLE_SEND, WAYBILL_UUID
} from './waybill.constants';
import {
  TCreateFirstTitleRequest, TCreateFirstTitleResponse, TTitleSendRequest, TUuidResponse,
  TWaybillSearchResponse,
  WaybillFilters,
  Waybill,
  TCloseRequest
} from './waybill.types';

export const WAYBILL_SEARCH_KEY = 'waybillSearch';
export const WAYBILL_DETAILED_KEY = 'waybillDetailed';

declare module 'api' {
  interface Cache {
    waybillSearch: {
      key: [typeof WAYBILL_SEARCH_KEY, WaybillFilters];
      value: TWaybillSearchResponse;
    };
    waybillDetailed: {
      key: [typeof WAYBILL_DETAILED_KEY, UUID];
      value: Waybill;
    };
  }
}

/** Запрос на получение детальной информации о ЭПЛ по ewbId */
export const useWaybill = (ewbId: UUID, config?: QueryConfig<Waybill>) => {
  return (
    useAPI(
      [WAYBILL_DETAILED_KEY, ewbId],
      ({ http, process }) => (
        http
          .get<Waybill>(WAYBILL_ONE, { urlParams: { ewbId } })
          .then(process.decodeResponseData(Waybill))
      ),
      {
        keepPreviousData: true,
        ...config,
      }
    )
  );
};

/** Запрос на получение ЭПЛ по параметрам */
export const useSearchWaybill = (
  filters: WaybillFilters,
  config?: QueryConfig<TWaybillSearchResponse>
) => {
  const {
    page, size, ...rest
  } = filters;

  return (
    useAPI(
      [WAYBILL_SEARCH_KEY, filters],
      ({ http, process }) => (
        http
          .post<TWaybillSearchResponse>(WAYBILL_SEARCH, {
            pageSetting: {
              page,
              size,
            },
            ...rest,
          })
          .then(process.getResponseData)
      ),
      {
        keepPreviousData: true,
        ...config,
      }
    )
  );
};

export const useGetUUID = (): MutationResultPair<
  TUuidResponse,
  AxiosError<Error>,
  unknown,
  unknown
> => (
  useAPIMutation(({ http, process }) => (
    http
      .post<TUuidResponse>(WAYBILL_UUID, {})
      .then<TUuidResponse>(process.getResponseData)
  ), {
    onSuccess: ignore,
    onError: ({ error, logger }) => {
      logger.toNotify('error', error.response?.data.message || error.message, getErrorMessage(error));
    },
  })
);

export const useCreateFirstTitle = (): MutationResultPair<
  TCreateFirstTitleResponse,
  AxiosError<Error>,
  TCreateFirstTitleRequest,
  unknown
> => (
  useAPIMutation(({ http, process }, data) => (
    http
      .post<TCreateFirstTitleResponse>(WAYBILL_FIRST_TITLE, data)
      .then<TCreateFirstTitleResponse>(process.getResponseData)
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

export const useSendTitle = (): MutationResultPair<
  unknown,
  AxiosError<Error>,
  TTitleSendRequest,
  unknown
> => (
  useAPIMutation(({ http, process }, data) => (
    http
      .post(WAYBILL_TITLE_SEND, data)
      .then(process.getResponseData)
  ), {
    onSuccess: ({ logger, t }) => {
      const { api: i18 } = t.Waybill.create;
      logger.toNotify('success', i18.success.description, i18.success.title);
    },
    onError: ({ error, logger }) => {
      logger.toNotify('error', error.response?.data.message || error.message, getErrorMessage(error));
    },
  })
);

export const useCloseEwb = (): MutationResultPair<
  unknown,
  AxiosError<Error>,
  TCloseRequest,
  unknown
> => (
  useAPIMutation(({ http, process }, data) => (
    http
      .post(WAYBILL_CLOSE, data)
      .then(process.getResponseData)
  ), {
    onSuccess: ({ logger, t }) => {
      const { api: i18n } = t.Waybill.modal.close;
      logger.toNotify('success', i18n.success.description, i18n.success.title);
    },
    onError: ({ error, logger }) => {
      logger.toNotify('error', error.response?.data.message || error.message, getErrorMessage(error));
    },
  })
);

export const useCancelEwb = (ewbId: string, humanReadableId: string): MutationResultPair<
  void,
  AxiosError<Error>,
  string,
  unknown
> => (
  useAPIMutation(({ http, process }, comment) => (
    http
      .patch<void>(WAYBILL_CANCEL, { comment }, { urlParams: { ewbId } })
      .then<void>(process.getResponseData)
  ), {
    onSuccess: ({ logger, t }) => {
      const { api: i18n } = t.Waybill.modal.cancel;
      logger.toNotify('success', i18n.success.description, i18n.success.title({ humanReadableId }));
    },
    onError: ({ error, logger }) => {
      logger.toNotify('error', error.response?.data.message || error.message, getErrorMessage(error));
    },
  })
);

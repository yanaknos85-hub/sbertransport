import { MOCKED_API_PREFIX } from 'constants/constants.api';
import { useAPI, useAPIMutation, APIQueryResult } from 'api';
import { AxiosError } from 'axios';
import { MutationResultPair, QueryConfig } from 'react-query';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { getErrorMessage } from 'utils';
import { EtrnCardDtoType, SigningEligibilityResponseType } from 'modules/Planner/Components/EtrnSignature/components/Card/types';
import {
  EtrnFiltersType,
  EtrnTitle,
  EtrnTitleDto,
  SearchEtrnResponse,
  SearchEtrnResponseType
} from 'modules/Planner/Components/EtrnSignature/types';

import { TitleType } from 'modules/Planner/Components/EtrnSignature/constants';
import {
  ETRN_CARGO_CARD,
  ETRN_CARGO_ELIGIBILITY,
  ETRN_CARGO_LOCK,
  ETRN_CARGO_LIST,
  ETRN_CARGO_TITLE,
  ETRN_CARGO_TITLE_SEND
} from 'constants/constants.api';
import {
  ETRN_CARD_CACHE_KEY,
  ETRN_ELIGIBILITY_CACHE_KEY,
  ETRN_SEARCH_CACHE_KEY,
  ETRN_TITLE_CACHE_KEY
} from './constants';

interface SendEtrnRequest {
  fileName: string;
  file: string;
  signature: string;
  creationTime: string;
}

interface SendEtrnArgs extends SendEtrnRequest {
  cardId: string;
  titleType: typeof TitleType.T3;
}

interface SendEtrnResult {
  success: true;
}

declare module 'api' {
  interface Cache {
    etrnCard: {
      key: [typeof ETRN_CARD_CACHE_KEY, string];
      value: EtrnCardDtoType;
    };
    etrnEligibility: {
      key: [typeof ETRN_ELIGIBILITY_CACHE_KEY];
      value: SigningEligibilityResponseType;
    };
    etrnSearch: {
      key: [typeof ETRN_SEARCH_CACHE_KEY, Record<string, unknown> | undefined];
      value: SearchEtrnResponseType;
    };
    etrnTitle: {
      key: [typeof ETRN_TITLE_CACHE_KEY, string];
      value: EtrnTitle;
    };
  }
}

export const useEtrnCard = (
  id: string | null,
  config?: QueryConfig<EtrnCardDtoType, AxiosError>
): APIQueryResult<EtrnCardDtoType, AxiosError> => useAPI(
  [ETRN_CARD_CACHE_KEY, id as string],
  ({ http, process }) => http
    .get(ETRN_CARGO_CARD, { urlParams: { cardId: id as string } })
    .then(process.decodeResponseData()),
  {
    enabled: Boolean(id),
    retry: false,
    ...config,
  }
);

export const useGetEtrnTitle = (
  etrnId: string | null,
  config?: QueryConfig<EtrnTitle, AxiosError>
): APIQueryResult<EtrnTitle, AxiosError> => {
  const { logger } = useAppStoreContext();

  return useAPI(
    [ETRN_TITLE_CACHE_KEY, etrnId as string],
    ({ http, process }) => http
      .get<EtrnTitle>(`${MOCKED_API_PREFIX}${ETRN_CARGO_TITLE}`, { urlParams: { etrnId: etrnId as string } })
      .then(process.decodeResponseData(EtrnTitleDto)),
    {
      enabled: Boolean(etrnId),
      retry: false,
      onError: error => {
        logger.toMessage('error', getErrorMessage(error));
      },
      ...config,
    }
  );
};

export const useAcquireLock = (): MutationResultPair<
  EtrnCardDtoType,
  AxiosError,
  { cardId: string },
  unknown
> => useAPIMutation(
  ({ http, process }, { cardId }) => http
    .put(ETRN_CARGO_LOCK, {}, { urlParams: { cardId } })
    .then(process.getResponseData) as Promise<EtrnCardDtoType>,
  {
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    onSuccess: () => {},
  }
);

export const useReleaseLock = (): MutationResultPair<
  EtrnCardDtoType,
  AxiosError,
  { cardId: string },
  unknown
> => useAPIMutation(
  ({ http, process }, { cardId }) => http
    .delete(ETRN_CARGO_LOCK, { urlParams: { cardId } })
    .then(process.getResponseData) as Promise<EtrnCardDtoType>,
  {
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    onSuccess: () => {},
  }
);

export const useSearchEtrn = (
  query: EtrnFiltersType | undefined,
  config?: QueryConfig<SearchEtrnResponseType, unknown>
) => useAPI(
  [ETRN_SEARCH_CACHE_KEY, query],
  ({ http, process }) => http
    .post<SearchEtrnResponseType>(`${ETRN_CARGO_LIST}`, query || {}, {})
    .then(process.decodeResponseData(SearchEtrnResponse)),
  {
    keepPreviousData: true,
    retry: 3,
    ...config,
  }
);

export const useSendEtrn = (): MutationResultPair<
  SendEtrnResult,
  AxiosError<Error>,
  SendEtrnArgs,
  unknown
> => useAPIMutation(
  ({ http, process }, { cardId, ...request }) => http
    .post<SendEtrnResult>(
      `${MOCKED_API_PREFIX}${ETRN_CARGO_TITLE_SEND}`,
      request,
      { urlParams: { etrnId: cardId } }
    )
    .then(process.getResponseData),
  {
    onSuccess: ({ logger, t }) => {
      logger.toNotify('success', t.Etrn.signedSuccess, t.global.success);
    },
    onError: ({ error, logger }) => {
      logger.toNotify('error', error.response?.data.message || error.message, getErrorMessage(error));
    },
  }
);

export const useCheckSigningEligibility = (): MutationResultPair<
  SigningEligibilityResponseType,
  AxiosError,
  void,
  unknown
> => useAPIMutation(
  ({ http, process }) => http
    .get<SigningEligibilityResponseType>(ETRN_CARGO_ELIGIBILITY)
    .then(process.decodeResponseData()),
  {
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    onSuccess: () => {},
    onError: ({ error, logger }) => {
      logger.toMessage('error', getErrorMessage(error));
    },
  }
);

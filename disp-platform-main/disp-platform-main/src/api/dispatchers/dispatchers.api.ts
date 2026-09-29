import { MutationResultPair } from 'react-query';
import { AxiosError, AxiosResponse } from 'axios';
import { ILogger } from '@sber-sbertransport/mf-core';
import { Translation } from 'i18n';

import { APIQueryResult, useAPI, useAPIMutation } from 'api';
import {
  GET_CONTRACTOR_DISPATCHERS,
  GET_CONTRACTOR_DISPATCHER,
  CREATE_CONTRACTOR_DISPATCHER,
  EDIT_CONTRACTOR_DISPATCHER,
  DELETE_CONTRACTOR_DISPATCHER,
  TRIP_TRANSPORT
} from './dispatchers.constants';
import {
  ContractorDispatcher,
  ContractorDispatcherResponse,
  TripTransportFilters,
  TripTransports,
  UseSearchDispatchersProps
} from './dispatchers.types';
import { getErrorMessage, getErrorProblems, ignore } from 'utils/utils';

declare module 'api' {
  interface Cache {
    allDispatchers: {
      key: ['allDispatchers', UseSearchDispatchersProps];
      value: ContractorDispatcherResponse;
    };
    dispatcher: { key: ['dispatcher']; value: ContractorDispatcher };
  }
}

export const useAllDispatchers = ({
  contractorId,
  autoparkId,
  query = {},
}: UseSearchDispatchersProps): APIQueryResult<ContractorDispatcherResponse> => (
  useAPI(
    ['allDispatchers', {
      contractorId, autoparkId, query,
    } as UseSearchDispatchersProps],
    ({ http, process }) => (
      http
        .get<ContractorDispatcherResponse>(GET_CONTRACTOR_DISPATCHERS, {
          urlParams: { contractorId },
          params: {
            ...query, autoparkId, active: true,
          },
        })
        .then(process.decodeResponseData(ContractorDispatcherResponse))
    ),
    {
      keepPreviousData: true,
    }
  )
);

interface GetDispatcherParams {
  contractorId: string;
  dispId: string;
}

export const useDispatcher = ({ contractorId, dispId }: GetDispatcherParams): APIQueryResult<ContractorDispatcher> => (
  useAPI(['dispatcher'], ({ http, process }) => http
    .get<ContractorDispatcher>(GET_CONTRACTOR_DISPATCHER, { urlParams: { contractorId, dispId } })
    .then(process.decodeResponseData(ContractorDispatcher))
  )
);

export const useGetDispatcher = ({
  contractorId,
  dispId,
}: GetDispatcherParams): MutationResultPair<ContractorDispatcher, unknown, never, unknown> => useAPIMutation(
  ({ http }) => http
    .get<ContractorDispatcher>(GET_CONTRACTOR_DISPATCHER, { urlParams: { contractorId, dispId } })
    .then(res => res.data),
  { onSuccess: async () => ({} as ContractorDispatcher) }
);

const parseDispatcherCodeError = ({
  t, error, logger,
}: { t: Translation; error: AxiosError; logger: ILogger }) => {
  if (error.response?.status === 409) {
    const problems = getErrorProblems(error, ['phone', 'email']);

    if (problems[0] && t.DispatcherTable.uniqueDuplicateErrors[problems[0].field]) {
      return logger.toMessage('error', t.DispatcherTable.uniqueDuplicateErrors[problems[0].field]);
    }

    logger.toMessage('error', t.DispatcherTable.duplicateError);
  } else if (error.response?.status !== 500) {
    logger.toMessage('error', getErrorMessage(error));
  }
};

interface CreateParams { dispatcher: Omit<ContractorDispatcher, 'id'> }

export const useCreateDispatcher = ({
  contractorId,
}: {
  contractorId: string;
}): MutationResultPair<AxiosResponse<ContractorDispatcher>, unknown, CreateParams, unknown> => useAPIMutation(
  ({ http }, { dispatcher }: CreateParams) => (
    http.post(CREATE_CONTRACTOR_DISPATCHER, dispatcher, { urlParams: { contractorId }, hush: [409] })
  ),
  {
    onSuccess: ({
      cache, process, t,
    }) => {
      process.processStatus(200, t.DispatcherTable.updateComplete);
      cache.refetchQueries(['allDispatchers']);
    },
    onError: ({
      t, error, logger,
    }) => {
      parseDispatcherCodeError({
        t, logger,
        error: error as AxiosError,
      });
    },
  }
);

interface EditParams { dispId: string; dispatcher: ContractorDispatcher }

export const useEditDispatcher = ({
  contractorId,
}: {
  contractorId: string;
}): MutationResultPair<unknown, unknown, EditParams, unknown> => useAPIMutation(
  ({ http }, { dispId, dispatcher }: EditParams) => (
    http.put(EDIT_CONTRACTOR_DISPATCHER, dispatcher, { urlParams: { contractorId, dispId }, hush: [409] })
  ),
  {
    onSuccess: ({
      cache, process, t,
    }) => {
      process.processStatus(200, t.DispatcherTable.updateComplete);
      cache.refetchQueries(['allDispatchers']);
    },
    onError: ({
      t, error, logger,
    }) => {
      parseDispatcherCodeError({
        t,
        logger,
        error: error as AxiosError,
      });
    },
  }
);

interface DeleteParams { dispId: string }

export const useDeleteDispatcher = ({
  contractorId,
}: {
  contractorId: string;
}): MutationResultPair<unknown, unknown, DeleteParams, unknown> => useAPIMutation(
  ({ http }, { dispId }: DeleteParams) => (
    http.delete(DELETE_CONTRACTOR_DISPATCHER, { urlParams: { contractorId, dispId } })
  ),
  {
    onSuccess: ({
      cache, process, t,
    }) => {
      process.processStatus(200, t.DispatcherTable.deleteComplete);
      cache.refetchQueries(['allDispatchers']);
    },
  }
);

/** Пагинированный список доступных авто для брони */
export const useTripTransport = (): MutationResultPair<
  TripTransports,
  AxiosError,
  TripTransportFilters,
  unknown
> => useAPIMutation(
  ({ http, process }, filters) => http
    .get<TripTransports>(TRIP_TRANSPORT, {
      params: filters,
      headers: { 'x-version': 2 },
    })
    .then(process.decodeResponseData(TripTransports)),
  {
    onSuccess: ignore,
  }
);

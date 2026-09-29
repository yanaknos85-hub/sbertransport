import { MutationResultPair, QueryConfig } from 'react-query';
import { AxiosError } from 'axios';

import { APIQueryResult, useAPI, useAPIMutation } from 'api/';
import indexById from 'utils/indexById';
import { ignore } from 'utils/utils';
import { UUID } from 'utils/io-ts';

import {
  CacheSearchedTransport,
  CacheSearchTransport,
  CreateTransportRequest,
  DeleteRequest,
  EditTransportRequest,
  Transport,
  TransportSearchRequest,
  TransportSearchResponse
} from './transport.types';
import {
  TRANSPORT_DEACTIVATE, TRANSPORT_ONE, TRANSPORT_QUERY, TRANSPORT_SEARCH
} from './transport.constants';

declare module 'api' {
  interface Cache {
    transportSearch: { key: ['transportSearch', TransportSearchRequest]; value: CacheSearchTransport };
  }
}

const allSearchedVehicles2cache = (transportSearchResponse: TransportSearchResponse): CacheSearchedTransport => ({
  response: transportSearchResponse,
  byId: indexById(transportSearchResponse.content),
});

export const useSearchTransport = (
  data: TransportSearchRequest,
  queryConfig?: QueryConfig<CacheSearchedTransport, AxiosError>
): APIQueryResult<CacheSearchedTransport, Error> => (
  useAPI(
    ['transportSearch', data],
    ({ http, process }) => http
      .post<TransportSearchResponse>(TRANSPORT_SEARCH, data)
      .then(process.decodeResponseData(TransportSearchResponse))
      .then(allSearchedVehicles2cache)
      .catch(() => ({} as CacheSearchedTransport)),
    {
      keepPreviousData: true,
      ...queryConfig,
    }
  )
);

export const useCreateTransport = (): MutationResultPair<Transport, AxiosError, CreateTransportRequest, unknown> => (
  useAPIMutation(
    ({ http, process }, data) => (
      http
        .post<Transport>(TRANSPORT_QUERY, CreateTransportRequest.encode(data))
        .then(process.decodeResponseData(Transport))
    ),
    {
      onSuccess: ({
        cache, process, t,
      }) => {
        process.processStatus(200, t.Vehicles.successCreateVehicle);
        cache.refetchQueries(['transportSearch']);
      },
      onError: ({ error, logger }) => {
        const errorDescription = error.response?.data.message || error.response?.data.detail
          || error.response?.data.error || error.message;
        logger.toMessage('error', errorDescription);
      },
    }
  )
);

export const useTransport = (): MutationResultPair<Transport, AxiosError, string, unknown> => (
  useAPIMutation(
    ({ http, process }, transportId) => (
      http
        .get<Transport>(TRANSPORT_ONE, { urlParams: { transportId } })
        .then(process.decodeResponseData(Transport))
    ),
    {
      onSuccess: ignore,
      onError: ({ error, logger }) => {
        const errorDescription = error.response?.data.message || error.response?.data.error || error.message;
        logger.toMessage('error', errorDescription);
      },
    }
  )
);

export const useDeactivateTransport = (transportId: UUID): MutationResultPair<
  Transport,
  AxiosError,
  DeleteRequest,
  unknown
> => (
  useAPIMutation(
    ({ http, process }, query) => (
      http
        .patch<void>(TRANSPORT_DEACTIVATE, DeleteRequest.encode(query), { urlParams: { transportId } })
        .then(process.decodeResponseData())
    ),
    {
      onSuccess: ({
        cache, process, t,
      }) => {
        process.processStatus(200, t.Vehicles.transportEditError);
        cache.refetchQueries(['transportSearch']);
      },
      onError: ({ error, logger }) => {
        const errorDescription = error.response?.data.message || error.response?.data.error || error.message;
        logger.toMessage('error', errorDescription);
      },
    }
  )
);

export const useEditTransport = (transportId: UUID): MutationResultPair<
  Transport,
  AxiosError,
  EditTransportRequest,
  unknown
> => (
  useAPIMutation(
    ({ http, process }, query) => (
      http
        .patch<Transport>(TRANSPORT_ONE, EditTransportRequest.encode(query), { urlParams: { transportId } })
        .then(process.decodeResponseData())
    ),
    {
      onSuccess: ({
        cache, process, t,
      }) => {
        process.processStatus(200, t.Vehicles.successEditVehicle);
        cache.refetchQueries(['transportSearch']);
      },
      onError: ({ error, logger }) => {
        const errorDescription = error.response?.data.message || error.response?.data.error || error.message;
        logger.toMessage('error', errorDescription);
      },
    }
  )
);

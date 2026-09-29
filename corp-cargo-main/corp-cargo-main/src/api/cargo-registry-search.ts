import { MutationResultPair, QueryConfig } from 'react-query';
import * as t from 'io-ts';
import { AxiosError } from 'axios';
import {
  SearchResponse,
  SearchRequest,
  TransportType,
  TripInfoDetailed,
  CargoRegistryFilters,
  Filters
} from 'stores/CargoRegistry/CargoRegistry.interface';
import {
  CARGO_REPORT,
  CARGO_REPORT_EXECUTOR,
  GET_CARGO_TRANSPORT_TYPES,
  GET_CARGO_TRIP_STATUS,
  GET_CARGO_USERS_ATTRIBUTES,
  GET_DEFAULT_CARGO_USERS_ATTRIBUTES
} from 'constants/constants.api';
import { UUID } from '../utils/io-ts';
import { EXECUTOR_GROUP_ALL_ID } from '../constants/constants.app';
import { APIQueryResult, useAPI, useAPIMutation } from './index';
import { TripStatus } from './travel-status';
import { UsersAttributes } from './register-search';
import { ColumnVisibilitySettings } from '../modules/CargoRegistry/types';

declare module 'api' {
  interface Cache {
    cargoRegister: { key: ['cargoRegister', SearchRequest]; value: SearchResponse };
    cargoTransportTypes: { key: ['cargoTransportTypes']; value: TransportType[] };
    cargoTripStatuses: { key: ['cargoTripStatuses']; value: TripStatus[] };
    cargoTripInfoDetailed: { key: ['cargoTripInfoDetailed']; value: TripInfoDetailed };
    defaultCargoColumns: { key: ['defaultCargoColumns']; value: UsersAttributes };
    userCargoSettings: { key: ['userCargoSettings', string]; value: UsersAttributes };
  }
}

const raw2JournalCache = (responseData: SearchResponse) => ({ responseData });

export const useSearchCargoRegistry = (
  query: SearchRequest,
  config: QueryConfig<SearchResponse, unknown>,
  orgId: string | null | undefined
): APIQueryResult<SearchResponse, AxiosError | unknown> => useAPI(
  ['cargoRegister', query],
  ({ http, process }) => orgId
    ? http
      .post<SearchResponse>(CARGO_REPORT, SearchRequest.encode(query), {
        urlParams: { orgId },
      })
      .then(process.decodeResponseData(SearchResponse))
    : ({} as SearchResponse),
  config
);

export const useRegistryJournalDataDeffered = (
  orgId: string | null | undefined
): MutationResultPair<CargoRegistryFilters, unknown, CargoRegistryFilters, unknown> => useAPIMutation(
  // @ts-ignore
  ({ http, process }, params) => orgId
    ? http
      .post<SearchResponse>(
        CARGO_REPORT_EXECUTOR,
        Filters.encode({ ...params, organizationId: orgId as UUID })
      )
      .then(process.decodeResponseData(SearchResponse))
      .then(raw2JournalCache)
    : ({} as SearchResponse),
  {
    onSuccess: ({ process }) => {
      process.decodeResponseData(SearchResponse);
    },
  }
);

export const useRegistryJournalDataExecutorDeffered = (
  execId?: string[] | undefined,
  emptyExecutorGroup?: boolean
): MutationResultPair<CargoRegistryFilters, unknown, CargoRegistryFilters, unknown> => useAPIMutation(
  // @ts-ignore
  ({ http, process }, params) => (execId || emptyExecutorGroup)
    ? http
      .post<SearchResponse>(
        CARGO_REPORT_EXECUTOR,
        Filters.encode({
          ...params,
          executorGroupIds: ((execId ?? []) as UUID[]).filter(
            id => id !== EXECUTOR_GROUP_ALL_ID
          ),
          emptyExecutorGroup: emptyExecutorGroup ?? false,
        })
      )
      .then(process.decodeResponseData(SearchResponse))
      .then(raw2JournalCache)
    : ({} as SearchResponse),
  {
    onSuccess: ({ process }) => {
      process.decodeResponseData(SearchResponse);
    },
  }
);

export const useCargoTransportTypes = (): APIQueryResult<TransportType[], AxiosError> => useAPI(['cargoTransportTypes'], ({ http, process }) => http.get<TransportType[]>(GET_CARGO_TRANSPORT_TYPES).then(process.decodeResponseData(t.array(TransportType)))
);

export const useCargoTripStatuses = (): APIQueryResult<TripStatus[], AxiosError> => useAPI(['cargoTripStatuses'], ({ http, process: { decodeResponseData } }) => http.get<TripStatus[]>(GET_CARGO_TRIP_STATUS).then(decodeResponseData(t.array(TripStatus)))
);

export const useCargoTripInfo = (id: string): APIQueryResult<TripInfoDetailed, AxiosError> => useAPI(['cargoTripInfoDetailed'], ({ http, process: { decodeResponseData } }) => http.get<TripInfoDetailed>(`/request-cargo/cargo/${id}`).then(decodeResponseData(TripInfoDetailed))
);

export const useSaveCargoUsersAttributes = (
  userId: string
): MutationResultPair<UsersAttributes, unknown, ColumnVisibilitySettings, unknown> => useAPIMutation(
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  ({ http, process }, settingsAttribute): Promise<any> => http.put<void>(`/reports-cargo/${userId}/attributes/cargo`, settingsAttribute).then(process.decodeResponseData()),
  {
    onSuccess: ({ cache }) => {
      cache.refetchQueries(['settingsAttributes']);
    },
  }
);

export const useDefaultCargoColumnVisibilitySettings = (): APIQueryResult<UsersAttributes, AxiosError> => useAPI(['defaultCargoColumns'], ({ http, process }) => http.get<UsersAttributes>(GET_DEFAULT_CARGO_USERS_ATTRIBUTES).then(process.decodeResponseData(UsersAttributes))
);

export const useCargoUserSettings = (userId: string): APIQueryResult<UsersAttributes, AxiosError> => useAPI(['userCargoSettings', userId], ({ http, process }) => http
  .get<UsersAttributes>(GET_CARGO_USERS_ATTRIBUTES, { urlParams: { userId } })
  .then(process.decodeResponseData(UsersAttributes))
);

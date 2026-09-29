import { APIQueryResult, useAPI, useAPIMutation } from 'api';
import {
  GET_ALL_USERS_ATTRIBUTES,
  GET_PERSONAL_TRIP_STATUS,
  PERSONAL_REPORT,
  SAVE_PERSONAL_USERS_ATTRIBUTES
} from 'constants/constants.api';
import { MutationResultPair } from 'react-query';
import {
  Filters,
  PersonalRegistryFilters,
  PersonalSearchResponse,
  SearchedPersonalCoopTrips
} from 'stores/PersonalSearch/PersonalSearch.interface';

import * as t from 'io-ts';
import { UUID } from 'utils/io-ts';
import { UsersAttributes } from './register-search';
import { TripStatus } from './travel-status';

declare module 'api' {
  interface Cache {
    personalTrips: {
      key: ['personalTrips', PersonalRegistryFilters, string | null | undefined];
      value: PersonalSearchResponse;
    };
    usersAttributes: { key: ['settingsAttributes', string]; value: UsersAttributes };
    personalTripStatuses: { key: ['personalTripStatuses']; value: TripStatus[] };
    searchPersonalCoopTrips: { key: ['searchPersonalCoopTrips', string[]]; value: SearchedPersonalCoopTrips };
  }
}

export const usePersonalSearch = (params: PersonalRegistryFilters, orgId: string | null | undefined) => useAPI(['personalTrips', params, orgId], ({ http, process }) => orgId
  ? http
    .post<PersonalSearchResponse>(PERSONAL_REPORT, Filters.encode({ ...params, organizationId: orgId as UUID }), {
      urlParams: { orgId },
    })
    .then(process.decodeResponseData(PersonalSearchResponse))
  : ({} as PersonalSearchResponse)
);

export const useUserSettings = (userId: string): APIQueryResult<UsersAttributes, Error> => useAPI(['userSettings', userId], ({ http, process }) => http
  .get<UsersAttributes>(GET_ALL_USERS_ATTRIBUTES, { urlParams: { userId } })
  .then(process.decodeResponseData(UsersAttributes))
);

export const useSaveUsersAttributes = (
  userId: string
): MutationResultPair<UsersAttributes, unknown, UsersAttributes, unknown> => useAPIMutation(
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  ({ http, process }, settingsAttribute): Promise<any> => http
    .put<void>(SAVE_PERSONAL_USERS_ATTRIBUTES, settingsAttribute, { urlParams: { userId } })
    .then(process.decodeResponseData()),
  {
    onSuccess: ({ cache }) => {
      cache.refetchQueries(['settingsAttributes']);
    },
  }
);

export const useGetPersonalTripStatuses = (): APIQueryResult<TripStatus[], Error> => useAPI(['personalTripStatuses'], ({ http, process }) => http.get<TripStatus[]>(GET_PERSONAL_TRIP_STATUS).then(process.decodeResponseData(t.array(TripStatus)))
);

export const useSearchPersonalCoopTrips = (
  orgId: string,
  query: string[]
): APIQueryResult<SearchedPersonalCoopTrips, Error> => useAPI(['searchPersonalCoopTrips', query], ({ http, process }) => Promise.all(
  query.map(id => http
    .post<PersonalSearchResponse>(
      PERSONAL_REPORT,
      { sharedRideId: id, organizationId: orgId },
      { urlParams: { orgId } }
    )
    .then(process.decodeResponseData(PersonalSearchResponse))
    .then(response => ({ sharedRideId: id, trips: response.content }))
  )
)
);

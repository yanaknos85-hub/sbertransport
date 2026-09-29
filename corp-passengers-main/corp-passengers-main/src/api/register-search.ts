import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

import {
  APIQueryResult, useAPI, useAPIMutation
} from 'api';
import { TripRequest } from 'stores/Trip/Trip.interface';
import { MutationResultPair, QueryConfig } from 'react-query';
import {
  GET_ALL_USERS_ATTRIBUTES, GET_TAXI_TRIP_STATUS, TAXI_REPORT, TAXI_REPORT_EXECUTOR
} from 'constants/constants.api';
import {
  Pageable,
  TripInfoForReporting,
  DateRangeISO
} from 'stores/PublicRegistry/models/PublicRegistry.interface';
import { PersonalSearchResponse } from 'stores/PersonalSearch/PersonalSearch.interface';
import { TripStatus } from './travel-status';
import { SortSetting } from 'stores/Engineer/Models/Settings/SortSettings';
import { PageSettings } from 'stores/Engineer/Models/Settings/PageSettings';

export const User = t.intersection([
  t.strict({
    lastName: t.string,
    firstName: t.string,
    id: tt.uuid,
  }),
  t.partial({
    patronymic: t.string,
  }),
]);
const RegisterSearchResponse = t.type({
  content: t.array(TripInfoForReporting),
  pageable: Pageable,
  totalElements: t.number,
});

export type RegisterSearchResponse = t.TypeOf<typeof RegisterSearchResponse>;

export const PurposeOption = t.partial({
  id: tt.uuid,
  label: t.string,
  name: t.string,
});
export type PurposeOption = t.TypeOf<typeof PurposeOption>;

export const RangeNumber = t.strict({
  start: t.number,
  end: t.number,
});

export const TimeRange = t.strict({
  start: tt.time,
  end: tt.time,
});

export const CostRange = t.strict({
  start: tt.money,
  end: tt.money,
});

export type RangeNumber = t.TypeOf<typeof RangeNumber>;

export type TimeRange = t.TypeOf<typeof TimeRange>;

export type CostRange = t.TypeOf<typeof CostRange>;

export enum SortFields {
  PASSENGER_FULL_NAME = 'PASSENGER_FULL_NAME',
  DESIRED_DATE = 'DESIRED_DATE',
  EXPECTED_COST = 'EXPECTED_COST',
  REQUEST_ID = 'REQUEST_ID',
  LIMIT_ID = 'LIMIT_ID',
  CREATION_DATE = 'CREATION_DATE',
  REQUEST_HUMAN_ID = 'REQUEST_HUMAN_ID',
  HUMAN_READABLE_ID = 'HUMAN_READABLE_ID', // Добавлено для ремонтов, пока не будет поправлено на беке
  ORGANIZATION = 'ORGANIZATION_OFFICIAL_NAME',
}

export const RegisterSearchQuery = t.partial({
  requestHumanId: t.string,
  coopTrip: t.boolean,
  requestStatusSet: t.array(t.string),
  expectedCost: RangeNumber,
  economyPercent: RangeNumber,
  passengerCountSet: t.array(t.number),
  contractorSet: t.array(t.string),
  tariffIdSet: t.array(t.string),
  factDistance: RangeNumber,
  ratingMarkSet: t.array(t.number),
  creationDate: DateRangeISO,
  desiredDateRange: DateRangeISO,
  costCenter: t.string,
  employeeFIO: t.string,
  personnelNumber: t.string,
  departmentCode: t.string,
  sharedRideOwnerFIO: t.string,
  sharedRideId: t.string,
  sortSetting: SortSetting,
  pageSetting: PageSettings,
  organizationId: tt.uuid,
  executorGroupIds: t.array(tt.uuid),
  requestClosedDatetime: DateRangeISO,
});

export type User = t.TypeOf<typeof User>;

export const UsersAttributes = t.partial({
  id: tt.nullable(tt.uuid),
  user: User,
  carsharingUIVisibility: t.record(t.string, t.boolean),
  taxiUIVisibility: t.record(t.string, t.boolean),
  personalUIVisibility: t.record(t.string, t.boolean),
  publicUIVisibility: t.record(t.string, t.boolean),
  cargoUIVisibility: t.record(t.string, t.union([t.boolean, t.undefined])),
});

export type UsersAttributes = t.TypeOf<typeof UsersAttributes>;

export type RegisterSearchQuery = t.TypeOf<typeof RegisterSearchQuery>;

export interface SearchedTaxiCoopTrip { sharedRideId: string; trips: TripInfoForReporting[] }

export type SearchedTaxiCoopTrips = SearchedTaxiCoopTrip[];

declare module 'api' {
  interface Cache {
    searchRegister: { key: ['searchTaxiRegister', RegisterSearchQuery]; value: RegisterSearchResponse };
    usersAttributes: { key: ['settingsAttributes', string]; value: UsersAttributes };
    oneTrip: { key: ['oneTrip', string]; value: TripRequest };
    taxiTripStatus: { key: ['taxiTripStatus']; value: TripStatus[] };
    personalCoopTrip: { key: ['personalCoopTrip', number | undefined]; value: PersonalSearchResponse };
    searchTaxiCoopTrips: { key: ['searchTaxiCoopTrips', string[], string]; value: SearchedTaxiCoopTrips };
  }
}

export const useSearchTaxiRegister = (
  query: RegisterSearchQuery,
  config: QueryConfig<RegisterSearchResponse, unknown>,
  orgId: string | null | undefined
): APIQueryResult<RegisterSearchResponse, unknown> => useAPI(
  ['searchTaxiRegister', query],
  ({ http, process }) => orgId
    ? http
      .post<RegisterSearchResponse>(
        TAXI_REPORT,
        RegisterSearchQuery.encode({
          ...query, organizationId: orgId as tt.UUID,
        }),
        { urlParams: { orgId } }
      )
      .then(process.decodeResponseData(RegisterSearchResponse))
    : ({} as RegisterSearchResponse),
  config
);

export const useSearchTaxiRegisterExecutorDeffered = (
  execId?: string[] | undefined
): MutationResultPair<RegisterSearchQuery, unknown, RegisterSearchQuery, unknown> => useAPIMutation(
  // @ts-ignore
  ({ http, process }, query) => (execId)
    ? http
      .post<RegisterSearchResponse>(
        TAXI_REPORT_EXECUTOR,
        RegisterSearchQuery.encode({
          ...query, executorGroupIds: execId as tt.UUID[],
        })
      )
      .then(process.decodeResponseData(RegisterSearchResponse))
    : ({} as RegisterSearchResponse),
  {
    onSuccess: ({
      process,
    }) => {
      process.decodeResponseData(RegisterSearchResponse);
    },
  }
);

export const useSearchTaxiRegisterDeffered = (
  orgId: string | null | undefined
): MutationResultPair<RegisterSearchQuery, unknown, RegisterSearchQuery, unknown> => useAPIMutation(
  // @ts-ignore
  ({ http, process }, query) => orgId
    ? http
      .post<RegisterSearchResponse>(
        TAXI_REPORT,
        RegisterSearchQuery.encode({
          ...query, organizationId: orgId as tt.UUID,
        }),
        { urlParams: { orgId } }
      )
      .then(process.decodeResponseData(RegisterSearchResponse))
    : ({} as RegisterSearchResponse),
  {
    onSuccess: ({
      process,
    }) => {
      process.decodeResponseData(RegisterSearchResponse);
    },
  }
);

export const useGetUsersAllAttributes = (userId: string): APIQueryResult<UsersAttributes, Error> => useAPI(['settingsAttributes', userId], ({ http, process }) => http
  .get<UsersAttributes>(GET_ALL_USERS_ATTRIBUTES, { urlParams: { userId } })
  .then(process.decodeResponseData(UsersAttributes))
);
export const useGetTaxiTripStatus = (): APIQueryResult<TripStatus[], Error> => useAPI(['taxiTripStatus'], ({ http, process }) => http.get<TripStatus[]>(GET_TAXI_TRIP_STATUS).then(process.decodeResponseData(t.array(TripStatus)))
);

export const useSaveUsersAttributes = (
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  userId: string
): MutationResultPair<UsersAttributes, unknown, UsersAttributes, unknown> => useAPIMutation(
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  ({ http, process }, settingsAttribute): Promise<any> => http.put<void>(`/reports/attributes/taxi`, settingsAttribute).then(process.decodeResponseData()),
  {
    onSuccess: ({
      cache, process, t: tr,
    }) => {
      process.processStatus(200, tr.Forms.sharedRidesSearch.settings);
      cache.refetchQueries(['settingsAttributes']);
    },
  }
);

export const useSearchTaxiCoopTrips = (orgId: string, query: string[]): APIQueryResult<SearchedTaxiCoopTrips, Error> => useAPI(['searchTaxiCoopTrips', query, orgId], ({ http, process }) => Promise.all(
  query.map(id => http
    .post<RegisterSearchResponse>(
      TAXI_REPORT,
      { sharedRideId: id, organizationId: orgId },
      { urlParams: { orgId } }
    )
    .then(process.decodeResponseData(RegisterSearchResponse))
    .then(response => ({ sharedRideId: id, trips: response.content }))
  )
)
);

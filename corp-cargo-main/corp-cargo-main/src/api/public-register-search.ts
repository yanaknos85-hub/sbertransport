import { MutationResultPair } from 'react-query';
import * as t from 'io-ts';
import { UUID } from '../utils/io-ts/index';
import { APIQueryResult, useAPI, useAPIMutation } from './index';
import {
  GET_ALL_USERS_ATTRIBUTES,
  GET_DEPARTMENT_USER,
  GET_PUBLIC_TRIP_STATUS_REPORTS,
  PUBLIC_REPORT,
  REQUESTS
} from '../constants/constants.api';
import { UsersAttributes } from './register-search';
import {
  PublicTransportTypes,
  Filters,
  PaymentStateResponse,
  PublicRegistryFilters,
  SearchResponse,
  EmployeeSearchRequest
} from '../stores/PublicRegistry/models/PublicRegistry.interface';
import { SelectedRowData } from 'modules/PublicRegistry/hooks/useRowSelection';
import { TripStatus } from './travel-status';
import { Employee } from 'stores/Employee/Employee.interface';

declare module 'api' {
  interface Cache {
    responseData: { key: ['publicTrips', PublicRegistryFilters, string]; value: { responseData: SearchResponse } };
    userSettings: { key: ['userSettings', string]; value: UsersAttributes };
    publicTripStatus: { key: ['publicTripStatus']; value: TripStatus[] };
    publicTransportTypes: { key: ['publicTransportTypes']; value: PublicTransportTypes[] };
    searchUserByDepartments: { key: ['searchUserByDepartments']; value: Employee };
  }
}

const raw2JournalCache = (responseData: SearchResponse) => ({ responseData });

export const useRegistryJournalData = (params: PublicRegistryFilters, orgId: string) => useAPI(['publicTrips', params, orgId], ({ http, process }) => http
  .post<SearchResponse>(PUBLIC_REPORT, Filters.encode({ ...params, organizationId: orgId as UUID }), {
    urlParams: { orgId },
  })
  .then(process.decodeResponseData(SearchResponse))
  .then(raw2JournalCache)
);
export const useUserSettings = (userId: string) => useAPI(['userSettings', userId], ({ http, process }) => http
  .get<UsersAttributes>(GET_ALL_USERS_ATTRIBUTES, { urlParams: { userId } })
  .then(process.decodeResponseData(UsersAttributes))
);

export const useSaveUsersAttributes = (
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  userId: string
): MutationResultPair<UsersAttributes, unknown, UsersAttributes, unknown> => useAPIMutation(
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  ({ http, process }, settingsAttribute): Promise<any> => http.put<void>(`/reports/attributes/public`, settingsAttribute).then(process.decodeResponseData()),
  {
    onSuccess: ({ cache }) => {
      cache.refetchQueries(['settingsAttributes']);
    },
  }
);

export const useSavePaymentStatuses = () => useAPIMutation(
  ({ http, process }, data: SelectedRowData[]) => http
    .put<PaymentStateResponse>(`${REQUESTS}/paymentStates`, data)
    .then(process.decodeResponseData(PaymentStateResponse)),
  {
    onSuccess: ({ t, logger }) => {
      logger.toMessage('success', t.Forms.registryFilterFields.statusSaveSuccess);
    },
  }
);

export const usePublicTripStatuses = (): APIQueryResult<TripStatus[], Error> => useAPI(['publicTripStatus'], ({ http, process: { decodeResponseData } }) => http.get<TripStatus[]>(GET_PUBLIC_TRIP_STATUS_REPORTS).then(decodeResponseData(t.array(TripStatus)))
);

export const useSearchUserByDepartments = ({
  organizationId,
  departmentId,
  id,
}: EmployeeSearchRequest): APIQueryResult<Employee, Error> => useAPI(['searchUserByDepartments'], ({ http, process }) => http
  .get<Employee>(GET_DEPARTMENT_USER, {
    urlParams: {
      orgId: organizationId, depId: departmentId, userId: id,
    },
  })
  .then(process.decodeResponseData(Employee))
);

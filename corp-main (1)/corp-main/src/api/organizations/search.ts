import { APIQueryResult, useAPI, useAPIMutation } from 'api';
import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import {
  IDepartment,
  IDepartmentApiVariables, Organization, OrganizationResponse, PaginationParams
} from 'stores/Organizations/Organizations.interface';
import { DEPARTMENT_LIST, GET_ALL_ORGANIZATIONS, GET_ALL_ORGANIZATIONS_METRICS } from 'constants/constants.api';
import { MutationResultPair, QueryConfig } from 'react-query';
import { getErrorMessage, ignore } from 'utils';
import { AxiosError } from 'axios';

export const OrganizationSearchQuery = t.partial({
  officialName: t.string,
  address: t.string,
  easupId: t.string,
  tid: t.string,
  organizationCode: t.number,
  msrn: t.string,
  groupId: tt.uuid,
  organizationGroupId: tt.uuid,
  OrganizationSearchQuery: tt.uuid,
  page: t.number,
  size: t.number,
});

export const OrganizationSearchQueryWithPagination = t.intersection([OrganizationSearchQuery, PaginationParams]);

export type OrganizationSearchQuery = t.TypeOf<typeof OrganizationSearchQuery>;

export type OrganizationSearchQueryWithPagination = t.TypeOf<typeof OrganizationSearchQueryWithPagination>;

declare module 'api' {
  interface Cache {
    searchOrganization: {
      key: ['searchOrganization', OrganizationSearchQueryWithPagination];
      value: OrganizationResponse;
    };
    organizationProjection: {
      key: ['organizationProjection', OrganizationSearchQuery | undefined];
      value: Organization[];
    };
    organizationMetrics: {
      key: ['organizationMetrics', OrganizationSearchQuery | undefined];
      value: OrganizationResponse;
    };
  }
}

export const useSearchOrganization = (
  query: OrganizationSearchQueryWithPagination
): APIQueryResult<OrganizationResponse, Error> => useAPI(
  ['searchOrganization', query],
  ({ http, process }) => http
    .get<OrganizationResponse>(GET_ALL_ORGANIZATIONS, {
      params: query,
    })
    .then(process.decodeResponseData(OrganizationResponse)),
  {
    refetchOnMount: true, cacheTime: 1, ...query,
  }
);

export const useOrganizationProjection = (
  {
    query,
    pagination,
  }: {
    query?: OrganizationSearchQuery;
    pagination?: PaginationParams;
  },
  config?: QueryConfig<Organization[], Error>
): APIQueryResult<Organization[], Error> => useAPI(
  ['organizationProjection', query],
  ({ http, process }) => http
    .get<Organization[]>(GET_ALL_ORGANIZATIONS, {
      // На беке баг. С projection SELECT возвращается список без пагинации, но все равно только первые 20 элементов.
      // Поэтому стоит size: 200. На проде сильно нагружать это не будет, т.к. там организаций мало
      params: {
        ...query, ...pagination, projection: 'SELECT', page: 0, size: 200,
      },
    })
    .then(process.decodeResponseData()),
  { refetchOnMount: true, ...config }
);

export const useOrganizationProjectionMetrics = (
  {
    query,
    pagination,
  }: {
    query?: OrganizationSearchQuery;
    pagination?: PaginationParams;
  },
  config?: QueryConfig<OrganizationResponse, Error>
): APIQueryResult<OrganizationResponse, Error> => useAPI(
  ['organizationMetrics', query],
  ({ http, process }) => http
    .get<Organization[]>(GET_ALL_ORGANIZATIONS_METRICS, {
      params: {
        ...query, ...pagination,
        organizationGroupId: query?.groupId, page: 0, size: query?.size,
      },
    })
    .then(process.decodeResponseData()),
  { refetchOnMount: true, ...config }
);

export const useGetDepartmentList = (
  organizationId?: string
): MutationResultPair<IDepartment[], AxiosError<Error>, IDepartmentApiVariables, unknown> => (
  useAPIMutation(
    // @ts-ignore
    ({ http, process }) => (organizationId)
      ? http
        .get<IDepartment[]>(DEPARTMENT_LIST, {
          urlParams: { organizationId: organizationId },
        })
        .then<IDepartment[]>(process.getResponseData)
      : ([] as IDepartment[]),
    {
      onSuccess: ignore,
      onError: ({ error, logger }) => {
        logger.toNotify('error', error.response?.data.message || error.message, getErrorMessage(error));
      },
    })
);


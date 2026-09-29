import { APIQueryResult, useAPI } from 'api';
import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import {
  Organization, OrganizationResponse, PaginationParams, SearchOrganizationsResponse
} from 'stores/Organizations/Organizations.interface';
import { GET_ALL_ORGANIZATIONS, GET_ALL_ORGANIZATIONS_SEARCH_TERM } from 'constants/constants.api';
import { QueryConfig } from 'react-query';

export const OrganizationSearchQuery = t.partial({
  officialName: t.string,
  address: t.string,
  easupId: t.string,
  tid: t.string,
  organizationCode: t.number,
  msrn: t.string,
  groupId: tt.uuid,
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
    organizationsSearchTerm: {
      key: ['organizationsSearchTerm', string];
      value: SearchOrganizationsResponse;
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

export const useOrganizationSearch = (
  organizationName: string,
  config?: QueryConfig<SearchOrganizationsResponse, Error>
): APIQueryResult<SearchOrganizationsResponse, Error> => useAPI(
  ['organizationsSearchTerm', organizationName],
  ({ http, process }) => http.get<SearchOrganizationsResponse>(GET_ALL_ORGANIZATIONS_SEARCH_TERM, {
    params: {
      organizationName,
    },
  }).then(process.decodeResponseData()), { ...config });

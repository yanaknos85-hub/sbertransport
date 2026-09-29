import {
  APIQueryResult, TypeAtKey, useAPI, useAPIMutation
} from 'api';
import { GET_ALL_ORGANIZATIONS, ORGANIZATIONS, ORGANIZATIONS_ID } from 'constants/constants.api';
import { FiltersOrganization, Organization, OrganizationResponse } from 'stores/Organizations/Organizations.interface';
import { getErrorMessage, ignore } from 'utils';
import indexById from 'utils/indexById';
import { UUID } from 'utils/io-ts';

import { mkUseUploadEntity } from 'api/upload';
import { QueryConfig } from 'react-query';

declare module 'api' {
  interface Cache {
    organizations: {
      key: ['organizations'];
      value: {
        organizationResponse: OrganizationResponse;
        byId: Record<string, Organization>;
      };
    };
    organization: { key: ['organization', UUID]; value: Organization };
  }
}

type OrganizationsCacheItem = TypeAtKey<['organizations']>;

const raw2cache = (organizationResponse: OrganizationResponse): OrganizationsCacheItem => ({
  organizationResponse,
  byId: indexById(organizationResponse.content),
});

export const useOrganizations = (): APIQueryResult<OrganizationsCacheItem, unknown> => useAPI(
  ['organizations'],
  ({ http, process }) => http
    .get<OrganizationResponse>(GET_ALL_ORGANIZATIONS)
    .then(process.decodeResponseData(OrganizationResponse))
    .then(raw2cache)
    .catch(
      () => ({
        organizations: [],
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      } as any)
    ),
  { cacheTime: 5 * 60 * 1000, staleTime: 5 * 60 * 1000 }
);

export const useOrganizationsWithParams = (
  params: FiltersOrganization
): APIQueryResult<OrganizationsCacheItem, unknown> => useAPI(
  ['organizations'],
  ({ http, process }) => http
    .get<OrganizationResponse>(GET_ALL_ORGANIZATIONS, { params })
    .then(process.decodeResponseData(OrganizationResponse))
    .then(raw2cache)
    .catch(
      () => ({
        organizations: [],
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      } as any)
    ),
  {
    cacheTime: 0, refetchOnMount: true, ...params,
  }
);

export const useCreateOrganization = () => useAPIMutation(
  ({ http, process }, org: Omit<Organization, 'id'>) => (
    // @ts-ignore
    http.post(`/${ORGANIZATIONS}/`, org, {}).then<Organization>(process.getResponseData)
  ), {
    onSuccess: ({
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      cache, result: organization, process, t,
    }) => {
      process.processStatus(200, t.Organizations.AddSuccess);
    },
    onError: ({
      error, logger, t,
    }) => {
      logger.toMessage(
        'error',
        error.response?.status === 409 ? t.Organizations.DuplicateError : getErrorMessage(error)
      );
    },
  }
);

export const useUpdateOrganization = () => useAPIMutation(
  ({ http }, organization: Organization) => http.put(`/${ORGANIZATIONS_ID}/`, organization, { urlParams: { orgId: organization.id } }).then(ignore),
  {
    onSuccess: ({
      cache, variables: organization, process, t,
    }) => {
      process.processStatus(200, t.Organizations.EditSuccess);
      cache.refetchQueries(['organization', organization.id]);
    },
  }
);

export const useDeleteOrganization = () => useAPIMutation(
  ({ http }, orgID: string) => http
    .delete<number>(`/${ORGANIZATIONS_ID}/`, { urlParams: { orgId: orgID } })
    .then(ignore),
  {
    onSuccess: ({
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      cache, variables: orgID, process, t,
    }) => {
      process.processStatus(200, t.Organizations.DeleteSuccess);
      cache.refetchQueries(['organizationProjection']);
    },
  }
);

export const useGetOrganizationById = (organizationId: UUID, config?: QueryConfig<Organization>) => useAPI(['organization', organizationId as UUID], ({ http, process }) => http
  .get<Organization>(`${ORGANIZATIONS_ID}/`, { urlParams: { orgId: organizationId } })
  .then(process.decodeResponseData(Organization)),
config
);

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const useUploadOrganizations = mkUseUploadEntity('organizations' as any, {
  onSuccess: ({ cache }) => cache.invalidateQueries(['organization']),
});

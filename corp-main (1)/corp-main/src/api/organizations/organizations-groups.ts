import { OrganizationsGroup, OrganizationsGroups } from 'stores/OrganizationsGroup/OrganizationsGroup.interface';
import { UUID } from 'utils/io-ts';
import { PaginationParams } from 'stores/Pagination/Pagination.interface';
import { APIQueryResult, useAPI, useAPIMutation } from '../index';
import { GET_ALL_ORGANIZATIONS_GROUPS, GET_ORGANIZATION_GROUP } from '../../constants/constants.api';
import { getErrorMessage, ignore } from '../../utils';

declare module 'api' {
  interface Cache {
    organizationsGroups: {
      key: ['organizationsGroups'];
      value: OrganizationsGroups;
    };
  }
}

export const useOrganizationsGroups = (
  { page, size }: PaginationParams = { page: 0, size: 200 }
): APIQueryResult<OrganizationsGroups, unknown> => (
  useAPI(['organizationsGroups'], ({ http, process }) => (
    http
      .get<OrganizationsGroups>(GET_ALL_ORGANIZATIONS_GROUPS, { params: { page, size } })
      .then(process.decodeResponseData(OrganizationsGroups))
  )
  )
);

export const useCreateOrganizationGroup = () => useAPIMutation(
  ({ http, process }, orgGroup: Omit<OrganizationsGroup, 'id'>) => http
    .post<OrganizationsGroup>(GET_ALL_ORGANIZATIONS_GROUPS, orgGroup)
    .then(process.decodeResponseData(OrganizationsGroup)),
  {
    onSuccess: ({
      cache, process, t,
    }) => {
      process.processStatus(200, t.contractors.addOrganizationGroupSuccess);
      cache.refetchQueries(['organizationsGroups']);
      setTimeout(() => cache.invalidateQueries(['organizationProjection']));
    },
    onError: ({ error, logger }) => {
      logger.toMessage('error', getErrorMessage(error));
    },
  }
);

export const useDeleteOrganizationsGroup = () => useAPIMutation(({ http }, groupId: UUID) => (
  http.delete(GET_ORGANIZATION_GROUP, { urlParams: { groupId } })
), {
  onSuccess: ({
    cache, process, t,
  }) => {
    process.processStatus(200, t.OrganizationGroups.organizationGroupDeleteSuccess);
    cache.refetchQueries(['organizationsGroups']);
  },
  onError: ({ t, logger }) => {
    logger.toMessage('error', t.Tariffs.InternalServerError);
  },
});

export const useEditOrganizationsGroup = () => useAPIMutation(
  ({ http }, data: OrganizationsGroup) => (
    http.put(GET_ORGANIZATION_GROUP, data, { urlParams: { groupId: data.id } }).then(ignore)
  ),
  {
    onSuccess: ({
      cache, process, t,
    }) => {
      process.processStatus(200, t.Organizations.EditSuccess);
      cache.refetchQueries(['organizationsGroups']);
      setTimeout(() => cache.invalidateQueries(['organizationProjection']));
    },
  }
);

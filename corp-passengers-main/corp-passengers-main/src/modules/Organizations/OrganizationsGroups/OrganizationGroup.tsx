import React, { FC } from 'react';

import { useOrganizationsGroups } from 'api/organizations/organizations-groups';
import { useQuery } from 'shared/hooks/useQuery';
import { Pagination } from 'shared/components/Pagination';
import { OrganizationsGroupsTable } from './OrganizationsGroupsTable';

export const OrganizationsGroup: FC = () => {
  const { query, setPagination } = useQuery();

  const { content, totalElements } = useOrganizationsGroups().data;

  return (
    <>
      <OrganizationsGroupsTable isFetching={false} organizationsGroups={content} />
      <Pagination
        pagination={query}
        setPagination={setPagination}
        total={totalElements}
      />
    </>
  );
};

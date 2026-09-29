import React, { Dispatch, FC, SetStateAction } from 'react';

import { useSearchOrganization } from 'api/organizations/search';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { Fields, Search } from './Search';
import { OrganizationsTable } from './OrganizationsTable';
import OrganizationPagination from './Pagination';

export const OrganizationsSearch: FC<{
  searchQuery: Fields;
  setSearchQuery: Dispatch<SetStateAction<Fields>>;
}> = ({ searchQuery, setSearchQuery }) => {
  const {
    data: { ...searchOrganization },
    isFetching,
  } = useSearchOrganization(searchQuery);

  return (
    <React.Suspense fallback={<SpinWrapped />}>
      <Search
        {...{ searchQuery, setSearchQuery }}
        organizations={searchOrganization.content}
        isFetching={isFetching}
      >
        {orgIds => (
          <>
            <OrganizationsTable
              isFetching={isFetching}
              organizations={searchOrganization.content.filter(({ id }) => orgIds.includes(id))}
            />
            <OrganizationPagination
              organizationResponse={searchOrganization}
              setSearchQuery={setSearchQuery}
              isFetching={isFetching}
            />
          </>
        )}
      </Search>
    </React.Suspense>
  );
};

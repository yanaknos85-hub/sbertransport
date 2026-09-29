import { useSearchDepartment } from 'api/departments/search';
import { useProfile } from 'api/profile';
import React from 'react';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';

import DepartmentPagination from './Components/Pagination';
import { Fields, Search } from './Components/Search';
import { DepartmentsTable } from './DepartmentsTable';

export const DepartmentSearch: React.FC<{
  searchQuery: Fields | null;
  setSearchQuery: (query: Fields) => void;
}> = ({ searchQuery, setSearchQuery }) => {
  const { organizationId } = useProfile().data;

  const {
    isFetching,
    data: { ...searchDepartment },
  } = useSearchDepartment({
    query: { ...searchQuery },
    pagination: { page: searchQuery?.page || 0, size: 20 },
    projection: 'FULL',
    // @ts-ignore
    orgId: organizationId,
  });

  return (
    <React.Suspense fallback={<SpinWrapped />}>
      <Search {...{ searchQuery, setSearchQuery }}>
        {departmentIds => (
          <>
            <DepartmentsTable
              isFetching={isFetching}
              departments={searchDepartment.content.filter(({ id }) => departmentIds.includes(id))}
            />
            <DepartmentPagination
              departmentsResponse={searchDepartment}
              searchQuery={searchQuery}
              isFetching={isFetching}
              setSearchQuery={setSearchQuery}
            />
          </>
        )}
      </Search>
    </React.Suspense>
  );
};

const Departments: React.FC = () => {
  const [searchQuery, setSearchQuery] = React.useState<Fields | null>(null);
  return <DepartmentSearch searchQuery={searchQuery} setSearchQuery={setSearchQuery} />;
};

export default Departments;

import React from 'react';
import { useSearchDepartment } from 'api/departments/search';
import { useUploadDepartments } from 'api/employee';
import { useProfile } from 'api/profile';

import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { StructureLoadAccess, useDownloadParams } from 'components/Structure/StructureLoadAccess';
import DownloadButton from 'components/DownloadButton';
import { ToolbarProvider } from 'components/Toolbar';
import { PanelTitle } from 'components/Panel';
import { UploadButton, importExportEndpointMap } from 'modules/UploadButton';

import DepartmentPagination from './Components/Pagination';
import { Fields, Search } from './Components/Search';
import { DepartmentsTable } from './DepartmentsTable';

export const DepartmentSearch: React.FC<{
  searchQuery: Fields | null;
  setSearchQuery: (query: Fields) => void;
}> = ({ searchQuery, setSearchQuery }) => {
  const { organizationId } = useProfile().data;
  const downloadParams = useDownloadParams();

  const {
    isFetching,
    data: { ...searchDepartment },
  } = useSearchDepartment({
    query: { ...searchQuery },
    pagination: { page: searchQuery?.page || 0, size: 20 },
    projection: 'FULL',
    orgId: organizationId,
  });

  return (
    <React.Suspense fallback={<SpinWrapped />}>
      <StructureLoadAccess>
        <UploadButton entity="department" useUpload={useUploadDepartments} />
        <DownloadButton url={`${importExportEndpointMap.department}/files/department`} params={downloadParams} />
      </StructureLoadAccess>
      <Search {...{ searchQuery, setSearchQuery }}>
        {departmentIds => (
          <>
            <DepartmentsTable
              isFetching={isFetching}
              departments={searchDepartment.content?.filter(({ id }) => departmentIds.includes(id))}
            />
            {!!searchDepartment.totalElements && (
              <DepartmentPagination
                departmentsResponse={searchDepartment}
                searchQuery={searchQuery}
                isFetching={isFetching}
                setSearchQuery={setSearchQuery}
              />
            )}
          </>
        )}
      </Search>
    </React.Suspense>
  );
};

const Departments: React.FC = () => {
  const [searchQuery, setSearchQuery] = React.useState<Fields | null>(null);

  return (
    <ToolbarProvider dataName="passenger_dir_departaments">
      <PanelTitle>Подразделения</PanelTitle>
      <DepartmentSearch searchQuery={searchQuery} setSearchQuery={setSearchQuery} />
    </ToolbarProvider>
  );
};

export default Departments;

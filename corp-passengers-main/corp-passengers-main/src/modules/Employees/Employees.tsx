import React, { FC, Suspense, useState } from 'react';
import { observer } from 'mobx-react';

import { useUploadEmployees } from 'api/employee';
import { useSearchEmployee } from 'api/employee/search';
import { useProfile } from 'api/profile';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { EmployeeStatus } from 'constants/constants.app';
import { StructureLoadAccess, useDownloadParams } from 'components/Structure/StructureLoadAccess';
import DownloadButton from 'components/DownloadButton';
import { ToolbarProvider } from 'components/Toolbar';
import { PanelTitle } from 'components/Panel';
import { UploadButton, importExportEndpointMap } from 'modules/UploadButton';

import EmployeePagination from './Components/Pagination';
import { Fields, Search } from './Components/Search';
import { EmployeesTable } from './EmployeesTable';

export const EmployeeSearch: FC<{
  searchQuery: Fields | null;
  setSearchQuery: (query: Fields) => void;
}> = ({ searchQuery, setSearchQuery }) => {
  const { organizationId } = useProfile().data;
  const {
    data: { ...searchEmployee },
  // @ts-ignore
  } = useSearchEmployee({ ...searchQuery }, { page: searchQuery?.page || 0, size: 20 }, organizationId);
  const downloadParams = useDownloadParams();

  return (
    <Suspense fallback={<SpinWrapped />}>
      <StructureLoadAccess>
        <UploadButton entity="employee" useUpload={useUploadEmployees} />
        <DownloadButton url={`${importExportEndpointMap.employee}/files/employee`} params={downloadParams} />
      </StructureLoadAccess>
      <Search {...{ searchQuery, setSearchQuery }}>
        {employeeIds => (
          <>
            <EmployeesTable employees={searchEmployee.content.filter(({ id }) => employeeIds.includes(id))} />
            <EmployeePagination
              employeeResponse={searchEmployee}
              searchQuery={searchQuery}
              setSearchQuery={setSearchQuery}
            />
          </>
        )}
      </Search>
    </Suspense>
  );
};

const Employees: FC = (): JSX.Element => {
  const [searchQuery, setSearchQuery] = useState<Fields | null>({
    status: EmployeeStatus.ACTIVE, // по-умолчанию показываем сразу только активные, потому что НЕ активные - НЕЛЬЗЯ редактировать!
  });
  return <EmployeeSearch searchQuery={searchQuery} setSearchQuery={setSearchQuery} />;
};

const withProvider = observer(() => (
  <ToolbarProvider dataName="passenger_dir_emloyees">
    <PanelTitle>Справочник «Сотрудники»</PanelTitle>
    <Employees />
  </ToolbarProvider>
));

export default withProvider;

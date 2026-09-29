import React, { FC, Suspense, useState } from 'react';
import { observer } from 'mobx-react';
import { useSearchEmployee } from 'api/employee/search';
import { useProfile } from 'api/profile';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { EmployeeStatus } from 'constants/constants.app';
import { ToolbarProvider } from 'components/Toolbar';
import { PanelTitle } from 'components/Panel';

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

  return (
    <Suspense fallback={<SpinWrapped />}>
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
  <ToolbarProvider>
    <PanelTitle>Справочник «Сотрудники»</PanelTitle>
    <Employees />
  </ToolbarProvider>
));

export default withProvider;

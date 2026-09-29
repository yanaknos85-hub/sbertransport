import React, {
  Dispatch, FC, SetStateAction, Suspense, useState
} from 'react';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { PublicRegistryFilters } from 'stores/PublicRegistry/models/PublicRegistry.interface';

import { PublicRegistryTable } from '../RegistryTable/PublicRegistryTable';

interface Props {
  filterParams?: PublicRegistryFilters;
  setFilterParams: Dispatch<SetStateAction<PublicRegistryFilters | undefined>>;
}

export const PublicRegistryJournal: FC<Props> = ({ filterParams, setFilterParams }) => {
  const [isStatusChangeActiveTable, setStatusChangeActiveTable] = useState(false);

  const renderTable = (): JSX.Element | undefined => filterParams && (
  <PublicRegistryTable
    filters={filterParams}
    setFilterParams={setFilterParams}
    isStatusChangeActive={isStatusChangeActiveTable}
    setStatusChangeActive={setStatusChangeActiveTable}
  />
  );

  return <Suspense fallback={<SpinWrapped />}>{renderTable()}</Suspense>;
};

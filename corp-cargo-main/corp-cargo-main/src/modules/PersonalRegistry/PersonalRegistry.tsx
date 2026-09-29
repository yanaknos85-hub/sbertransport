import React, {
  Dispatch, FC, SetStateAction, Suspense, useState
} from 'react';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { PersonalRegistryFilters } from 'stores/PersonalSearch/PersonalSearch.interface';
import { PersonalRegistryTable } from './components/PersonalRegistryTable';

interface Props {
  filterValues?: PersonalRegistryFilters;
  setFilterValues: Dispatch<SetStateAction<PersonalRegistryFilters | undefined>>;
}

export const PersonalRegistry: FC<Props> = ({ filterValues, setFilterValues }) => {
  const [isStatusChangeActiveTable, setStatusChangeActiveTable] = useState(false);

  return (
    <Suspense fallback={<SpinWrapped />}>
      {filterValues && (
        <PersonalRegistryTable
          filters={filterValues}
          setFilterParams={setFilterValues}
          isStatusChangeActive={isStatusChangeActiveTable}
          setStatusChangeActive={setStatusChangeActiveTable}
        />
      )}
    </Suspense>
  );
};

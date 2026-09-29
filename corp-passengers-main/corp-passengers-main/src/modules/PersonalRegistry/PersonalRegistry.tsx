import React, {
  Dispatch, FC, SetStateAction, Suspense
} from 'react';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { PersonalRegistryFilters } from 'stores/PersonalSearch/PersonalSearch.interface';
import { PersonalRegistryTable } from './components/PersonalRegistryTable';
import { useProfile } from 'api/profile';

interface Props {
  filterValues: PersonalRegistryFilters;
  setFilterValues: Dispatch<SetStateAction<PersonalRegistryFilters>>;
}

export const PersonalRegistry: FC<Props> = ({ filterValues, setFilterValues }) => {
  const {
    userId, organizationId, isOrganization, executorGroupId,
  } = useProfile().data;

  return (
    <Suspense fallback={<SpinWrapped />}>
      {filterValues && (
        <PersonalRegistryTable
          filters={filterValues}
          setFilterParams={setFilterValues}
          userId={userId}
          organizationId={organizationId}
          isOrganization={isOrganization}
          executorGroupId={executorGroupId}
        />
      )}
    </Suspense>
  );
};

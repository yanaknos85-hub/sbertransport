import React, {
  Dispatch, FC, SetStateAction, Suspense
} from 'react';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { PublicRegistryFilters } from 'stores/PublicRegistry/models/PublicRegistry.interface';

import { PublicRegistryTable } from '../RegistryTable/PublicRegistryTable';
import { useProfile } from 'api/profile';

interface Props {
  filterParams: PublicRegistryFilters;
  setFilterParams: Dispatch<SetStateAction<PublicRegistryFilters>>;
}

export const PublicRegistryJournal: FC<Props> = ({ filterParams, setFilterParams }) => {
  const {
    userId, organizationId, isOrganization, executorGroupId,
  } = useProfile().data;

  const renderTable = (): JSX.Element | undefined => filterParams && (
  <PublicRegistryTable
    filters={filterParams}
    setFilterParams={setFilterParams}
    userId={userId}
    organizationId={organizationId}
    isOrganization={isOrganization}
    executorGroupId={executorGroupId}
  />
  );

  return <Suspense fallback={<SpinWrapped />}>{renderTable()}</Suspense>;
};

import React, { FC, Suspense } from 'react';

import { useProfile } from 'api/profile/profile.api';
import { WaybillFilters as Filters } from 'api/waybill/waybill.types';

import { useQuery } from 'hooks/useQuery';

import { UUID } from 'utils/io-ts';

import ErrorBoundary from 'components/ErrorBoundary';
import SpinWrapped from 'components/SpinWrapped/SpinWrapped';

import WaybillFilters from './components/WaybillFilters';
import WaybillTable from './components/WaybillTable';

const Waybill: FC = () => {
  const { contractorId, autoparkId } = useProfile().data;

  const {
    query, setQuery, setPagination,
  } = useQuery<Filters>({
    contractorIds: [contractorId],
    autoparkIds: autoparkId ? [autoparkId as UUID] : undefined,
  });

  return (
    <>
      <ErrorBoundary>
        <WaybillFilters query={query} setQuery={setQuery} />
      </ErrorBoundary>
      <ErrorBoundary>
        <Suspense fallback={<SpinWrapped />}>
          <WaybillTable query={query} setPagination={setPagination} />
        </Suspense>
      </ErrorBoundary>
    </>
  );
};

export default Waybill;

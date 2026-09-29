import React, {
  Suspense, useState
} from 'react';
import moment from 'moment';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { PublicRegistryFilters } from 'stores/PublicRegistry/models/PublicRegistry.interface';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';

import { PublicBusinessReportsTable } from '../PublicBusinessReportsTable/PublicBusinessReportsTable';

export const PublicBusinessJournal = withErrorBoundary(() => {
  const defaultFilters = {
    orderPaymentFormationStartDate: {
      start: moment().startOf('month').format('YYYY-MM-DDTHH:mm:ss'),
      end: moment().format('YYYY-MM-DDTHH:mm:ss'),
    },
    pageSetting: { page: 0, size: 100 },
  } as PublicRegistryFilters;

  const [filterParams, setFilterParams] = useState<PublicRegistryFilters | undefined>(defaultFilters);

  const renderTable = (): JSX.Element | undefined => filterParams && (
  <PublicBusinessReportsTable
    filters={filterParams}
    setFilterParams={setFilterParams}
  />
  );

  return <Suspense fallback={<SpinWrapped />}>{renderTable()}</Suspense>;
});

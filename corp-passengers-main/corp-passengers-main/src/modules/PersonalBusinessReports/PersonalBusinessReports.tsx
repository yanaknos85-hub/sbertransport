import React, {
  Suspense, useState
} from 'react';
import moment from 'moment';

import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { PersonalRegistryFilters } from 'stores/PersonalSearch/PersonalSearch.interface';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';

import { PersonalBusinessReportTables } from './components/PersonalBusinessReportTables';

export const PersonalBusinessReports = withErrorBoundary(() => {
  const defaultFilters = {
    orderPaymentFormationStartRange: {
      start: moment().startOf('month').format('YYYY-MM-DDTHH:mm:ss'),
      end: moment().endOf('day').format('YYYY-MM-DDTHH:mm:ss'),
    },
    pageSetting: { page: 0, size: 100 },
  } as PersonalRegistryFilters;

  const [filterValues, setFilterValues] = useState<PersonalRegistryFilters | undefined>(defaultFilters);

  return (
    <Suspense fallback={<SpinWrapped />}>
      {filterValues && (
        <PersonalBusinessReportTables
          filters={filterValues}
          setFilterParams={setFilterValues}
        />
      )}
    </Suspense>
  );
});

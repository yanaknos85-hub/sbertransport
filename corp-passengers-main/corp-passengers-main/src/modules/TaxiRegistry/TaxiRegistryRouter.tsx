import React, { Suspense, useState } from 'react';
import { Route, Switch } from 'react-router-dom';
import { ToolbarProvider } from 'components/Toolbar';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import { TaxiRegistryFilter } from './types/types';
import { TaxiRegistryPage } from './TaxiRegistryPage';
import DetailedView from './Components/DetailedView/DetailedView';
import { usePagination } from './hooks/usePagination';
import moment from 'moment';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';

import * as routes from 'constants/constants.routes';

export const TaxiRegistryRouter = withErrorBoundary(() => {
  const defaultFilters = {
    desiredDateRange: {
      mode: 'range',
      value: [moment().startOf('month'), moment()],
    },
  } as TaxiRegistryFilter;

  const [filterValues, setFilterValues] = useState<TaxiRegistryFilter | undefined>(defaultFilters);
  const [hashTariffs, setHashTariffs] = useState<Record<string, string>>({});
  const {
    pageSetting, onPaginationChange, resetPagination,
  } = usePagination();
  const { [StoreNames.registryStore]: register } = useAppStoreContext();
  register.setSaveSettings([]);

  const pageOptions = {
    pageSetting,
    onPaginationChange,
  };

  return (
    <Switch>
      <ToolbarProvider>
        <Route path={routes.REGISTRY_PASSENGERS_TAXI} exact>
          <Suspense fallback={<SpinWrapped />}>
            <TaxiRegistryPage
              filterValues={filterValues}
              setFilterValues={setFilterValues}
              pageOptions={pageOptions}
              resetPagination={resetPagination}
              hashTariffs={hashTariffs}
              setHashTariffs={setHashTariffs}
            />
          </Suspense>
        </Route>
        <Route
          path={routes.REGISTRY_PASSENGERS_TAXI_VIEW}
          component={DetailedView}
          exact
        />
      </ToolbarProvider>
    </Switch>
  );
});

export default TaxiRegistryRouter;

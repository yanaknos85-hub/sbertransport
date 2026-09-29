import React, { Suspense, useState } from 'react';
import { Route, Switch } from 'react-router-dom';
import { ToolbarProvider } from 'components/Toolbar';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import { TaxiYandexRegistryFilter } from './types/types';
import { TaxiYandexRegistryPage } from './TaxiYandexRegistryPage';
import { DetailedView } from './components/DetailedView/DetailedView';
import { usePagination } from './hooks/usePagination';
import * as routes from 'constants/constants.routes';

export const TaxiYandexRegistryRouter = withErrorBoundary(() => {
  const [filterValues, setFilterValues] = useState<TaxiYandexRegistryFilter>({});
  const {
    pageSetting, onPaginationChange, resetPagination,
  } = usePagination();

  const pageOptions = {
    pageSetting,
    onPaginationChange,
  };

  return (
    <Switch>
      <ToolbarProvider>
        <Route path={routes.REGISTRY_PASSENGERS_YANDEX_TAXI} exact>
          <Suspense fallback={<SpinWrapped />}>
            <TaxiYandexRegistryPage
              filterValues={filterValues}
              setFilterValues={setFilterValues}
              pageOptions={pageOptions}
              resetPagination={resetPagination}
            />
          </Suspense>
        </Route>
        <Route
          path={routes.REGISTRY_PASSENGERS_YANDEX_TAXI_VIEW}
          component={DetailedView}
          exact
        />
      </ToolbarProvider>
    </Switch>
  );
});

export default TaxiYandexRegistryRouter;

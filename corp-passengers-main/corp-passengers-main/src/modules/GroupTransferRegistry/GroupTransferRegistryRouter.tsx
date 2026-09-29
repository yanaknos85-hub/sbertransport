import React, { Suspense, useState } from 'react';
import { Route, Switch } from 'react-router-dom';
import moment from 'moment';

import { ToolbarProvider } from 'components/Toolbar';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';

import { GroupTransferRegistryFilter } from './types/types';
import { GroupTransferRegistryPage } from './GroupTransferRegistryPage';
import GroupTransferDetailed from './Components/DetailedView/DetailedView';
import { usePagination } from './hooks/usePagination';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';

import * as routes from 'constants/constants.routes';

export const GroupTransferRegistryRouter = withErrorBoundary(() => {
  const defaultFilters = {
    desiredDateRange: {
      mode: 'range',
      value: [moment().startOf('month'), moment()],
    },
  } as GroupTransferRegistryFilter;

  const [filterValues, setFilterValues] = useState<GroupTransferRegistryFilter | undefined>(defaultFilters);
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
        <Route
          path={routes.REGISTRY_PASSENGERS_GROUP_TRANSFER}
          exact
        >
          <Suspense fallback={<SpinWrapped />}>
            <GroupTransferRegistryPage
              filterValues={filterValues}
              setFilterValues={setFilterValues}
              pageOptions={pageOptions}
              resetPagination={resetPagination}
            />
          </Suspense>
        </Route>
        <Route
          path={routes.REGISTRY_PASSENGERS_GROUP_TRANSFER_VIEW}
          component={GroupTransferDetailed}
          exact
        />
      </ToolbarProvider>
    </Switch>
  );
});

export default GroupTransferRegistryRouter;

import React, { useState } from 'react';
import { Route, Switch } from 'react-router-dom';
import { PublicRegistryFilters } from 'stores/PublicRegistry/models/PublicRegistry.interface';

import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import { DetailedView } from './DetailedView/DetailedView';
import { PublicRegistryJournal } from './RegistryJournal/PublicRegistryJournal';
import moment from 'moment';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';

import * as routes from 'constants/constants.routes';

export const PublicRegistryRouter = withErrorBoundary(() => {
  const defaultFilters = {
    orderPaymentFormationStartDate: { mode: 'range', value: [moment().startOf('month'), moment()] },
    pageSetting: { page: 0, size: 100 },
  } as unknown as PublicRegistryFilters;
  const { [StoreNames.registryStore]: register } = useAppStoreContext();
  register.setSaveSettings([]);

  const [filterParams, setFilterParams] = useState<PublicRegistryFilters>(defaultFilters);

  return (
    <Switch>
      <Route
        path={routes.REGISTRY_PASSENGERS_PUBLIC}
        render={() => <PublicRegistryJournal filterParams={filterParams} setFilterParams={setFilterParams} />}
        exact
      />
      <Route
        path={routes.REGISTRY_PASSENGERS_PUBLIC_VIEW}
        component={DetailedView}
        exact
      />
    </Switch>
  );
});

export default PublicRegistryRouter;

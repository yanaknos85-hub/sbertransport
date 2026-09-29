import React, { useState } from 'react';
import { Route, Switch } from 'react-router-dom';
import { PersonalRegistryFilters } from 'stores/PersonalSearch/PersonalSearch.interface';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import moment from 'moment';
import { PersonalRegistry } from './PersonalRegistry';
import PersonalTripDetailed from './components/DetailedView/DetailedView';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';

import * as routes from 'constants/constants.routes';

export const PersonalRegistryRouter = withErrorBoundary(() => {
  const defaultFilters = {
    orderPaymentFormationStartRange: { mode: 'range', value: [moment().startOf('month'), moment()] },
    pageSetting: { page: 0, size: 100 },
  } as unknown as PersonalRegistryFilters;
  const { [StoreNames.registryStore]: register } = useAppStoreContext();
  register.setSaveSettings([]);

  const [filterValues, setFilterValues] = useState<PersonalRegistryFilters>(defaultFilters);

  return (
    <Switch>
      <Route
        path={routes.REGISTRY_PASSENGERS_PERSONAL}
        render={() => <PersonalRegistry filterValues={filterValues} setFilterValues={setFilterValues} />}
        exact
      />
      <Route
        path={routes.REGISTRY_PASSENGERS_PERSONAL_VIEW}
        component={PersonalTripDetailed}
        exact
      />
    </Switch>
  );
});

export default PersonalRegistryRouter;

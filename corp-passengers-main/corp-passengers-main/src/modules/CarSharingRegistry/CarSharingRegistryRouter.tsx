import React from 'react';
import { Route, Switch } from 'react-router-dom';
import moment from 'moment';

import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import { RegistryFiltersProvider } from '../Registry/RegistryFilterContext';
import CarSharingRegistry from './CarSharingRegistry';
import { CarSharingReportFilters } from 'stores/CarSharingTrip/CarSharingTrip.interface';
import DetailedView from './components/DetailedView/DetailedView';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';

import * as routes from 'constants/constants.routes';

export const CarSharingRegistryRouter = withErrorBoundary(() => {
  const defaultFilters = {
    desiredDate: { mode: 'range', value: [moment().startOf('month'), moment()] },
  } as unknown as CarSharingReportFilters;
  const { [StoreNames.registryStore]: register } = useAppStoreContext();
  register.setSaveSettings([]);

  return (
    <RegistryFiltersProvider<CarSharingReportFilters> defaultFilters={defaultFilters}>
      <Switch>
        <Route
          path={routes.REGISTRY_PASSENGERS_CARSHARING}
          component={CarSharingRegistry}
          exact
        />
        <Route
          path={routes.REGISTRY_PASSENGERS_CARSHARING_VIEW}
          component={DetailedView}
          exact
        />
      </Switch>
    </RegistryFiltersProvider>
  );
});

export default CarSharingRegistryRouter;

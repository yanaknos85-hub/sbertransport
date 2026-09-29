import React, { FC, Suspense, lazy } from 'react';
import { Redirect, Route, Switch } from 'react-router-dom';
import { observer } from 'mobx-react';

import * as routes from 'constants/routes.constants';

import { CustomRoute } from 'components/Breadcrumbs/CustomRoutes';
import ErrorBoundary from 'components/ErrorBoundary';
import useErrorBoundary from 'hooks/useErrorBoundary';
import SpinWrapped from 'components/SpinWrapped/SpinWrapped';

const Vehicles = lazy(() => import('modules/Vehicles/VehiclesRouter'));
const AutoParks = lazy(() => import('modules/AutoParks/AutoParks'));
const Telematics = React.lazy(() => import('modules/Telematics/Telematics'));
const ReleaseOnLine = React.lazy(() => import('modules/ReleaseOnLine'));
const Maintenance = React.lazy(() => import('modules/Maintenance'));
const Waybill = React.lazy(() => import('modules/Waybill/WaybillRouter'));

export const AppRouter: FC = observer((): JSX.Element => {
  const { errorBoundaryRef } = useErrorBoundary();

  return (
    <ErrorBoundary ref={errorBoundaryRef} catchUnhandled>
      <Suspense fallback={<SpinWrapped />}>
        <Switch>
          <CustomRoute path={routes.VEHICLES} component={Vehicles} />
          <CustomRoute path={routes.AUTOPARKS} component={AutoParks} />
          <CustomRoute path={routes.TELEMATICS} component={Telematics} />
          <CustomRoute path={routes.RELEASE_ON_LINE} component={ReleaseOnLine} />
          <CustomRoute path={routes.MAINTENANCE} component={Maintenance} />
          <CustomRoute path={routes.WAYBILL} component={Waybill} />

          <Route path={routes.APP} component={() => <Redirect to={routes.VEHICLES} />} />
          <Route path="*" component={() => <Redirect to={routes.PAGE_404} />} />
        </Switch>
      </Suspense>
    </ErrorBoundary>
  );
});

export default AppRouter;

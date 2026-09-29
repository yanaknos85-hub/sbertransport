/* eslint-disable @stylistic/jsx-max-props-per-line */
import React, { FC, Suspense, lazy } from 'react';
import { Switch, Route, Redirect } from 'react-router-dom';
import { observer } from 'mobx-react';

import * as routes from 'constants/routes.constants';
import { TripType } from 'constants/app.constants';

import { CustomRoute } from 'components/Breadcrumbs/CustomRoutes';
import ErrorBoundary from 'components/ErrorBoundary';
import SpinWrapped from 'components/SpinWrapped/SpinWrapped';
import useErrorBoundary from 'hooks/useErrorBoundary';

import ProdRouter from '../prod/AppRouter';

const Trips = lazy(() => import('modules/Trips/FakePage/FakePage.TripsRouter'));

export const AppRouter: FC = observer((): JSX.Element => {
  const { errorBoundaryRef } = useErrorBoundary();

  return (
    <ErrorBoundary ref={errorBoundaryRef} catchUnhandled>
      <Suspense fallback={<SpinWrapped />}>
        <Switch>
          <CustomRoute path={routes.TRIPS} component={Trips} />

          <Route exact path="/" component={() => <Redirect to={`${routes.TRIPS}/${TripType.Passenger}`} />} />
          <Route exact path="/client" component={() => <Redirect to={`${routes.TRIPS}/${TripType.Passenger}`} />} />
          <ProdRouter />
        </Switch>
      </Suspense>
    </ErrorBoundary>
  );
});

export default AppRouter;

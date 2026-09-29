import React, { FC, lazy } from 'react';
import { Route, Switch, Redirect } from 'react-router-dom';

import { TRIPS } from 'constants/routes.constants';
import { TripType } from 'constants/app.constants';

const Trips = lazy(() => import('./FakePage.Trips'));

const TripsRouter: FC = () => {
  return (
    <Switch>
      <Route path={`${TRIPS}/:type`} component={Trips} />

      <Redirect path={TRIPS} to={`${TRIPS}/${TripType.Cargo}`} />
    </Switch>
  );
};

export default TripsRouter;

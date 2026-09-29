/* eslint-disable @stylistic/jsx-max-props-per-line */
import React, { FC } from 'react';
import { Route, Switch, Redirect } from 'react-router-dom';
import * as routes from 'constants/constants.routes';
import { ServicesTabs } from './constants/tripSettings';

import TransportTypes from './components/TransportTypes/TransportTypes';
import TripPurposes from './components/TripPurposes/TripPurposes';
import Approvals from './components/Approvals/ApprovalsSettings';
import SharedRideSettings from './components/SharedRideSettings/SharedRideSettings';

const defaultPath = ServicesTabs.Passengers;

export const Router: FC = (): JSX.Element => {
  return (
    <Switch>
      <Route path={`${routes.SERVICE_TYPES}/:type`} component={TransportTypes} />
      <Route path={`${routes.PURPOSES}/:type`} component={TripPurposes} />
      <Route path={`${routes.APPROVALS}/:type`} component={Approvals} />
      <Route path={`${routes.SHARED_RIDES}/:type`} component={SharedRideSettings} />

      <Route exact path={routes.TRIP_SETTINGS} component={() => <Redirect to={`${routes.SERVICE_TYPES}`} />} />
      <Route exact path={routes.SERVICE_TYPES} component={() => <Redirect to={`${routes.SERVICE_TYPES}/${defaultPath}`} />} />
      <Route exact path={routes.PURPOSES} component={() => <Redirect to={`${routes.PURPOSES}/${defaultPath}`} />} />
      <Route exact path={routes.APPROVALS} component={() => <Redirect to={`${routes.APPROVALS}/${defaultPath}`} />} />
      <Route exact path={routes.SHARED_RIDES} component={() => <Redirect to={`${routes.SHARED_RIDES}/${defaultPath}`} />} />
      <Route path="*" component={() => <Redirect to={routes.TRIP_SETTINGS} />} />
    </Switch>
  );
};

export default Router;

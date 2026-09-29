import React, { FC } from 'react';
import { Route, Switch, Redirect } from 'react-router-dom';
import * as routes from 'constants/constants.routes';
import Autopark from './Autopark';
import { AutoparkTabs } from './Autopark.constants';

const AutoparkRouter: FC = () => (
  <Switch>
    <Route
      path={routes.AUTOPARKS_TAB}
      component={Autopark}
    />
    <Route
      exact
      path="*"
      component={() => <Redirect to={routes.AUTOPARKS_TAB.replace(':autoparkTab', AutoparkTabs.List)} />}
    />
  </Switch>
);

export default AutoparkRouter;

/* eslint-disable @stylistic/jsx-max-props-per-line */
import React, { FC } from 'react';
import { Route, Switch, Redirect } from 'react-router-dom';
import * as routes from 'constants/constants.routes';

import Contractors from './Contractors';

export const Router: FC = (): JSX.Element => {
  return (
    <Switch>
      <Route path={`${routes.CUSTOMERS_CONTRACTORS}/:routeId`} component={Contractors} />

      <Route exact path={routes.CUSTOMERS_CONTRACTORS} component={() => <Redirect to={`${routes.CUSTOMERS_CONTRACTORS}/all`} />} />
    </Switch>
  );
};

export default Router;

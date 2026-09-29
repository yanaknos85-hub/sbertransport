/* eslint-disable @stylistic/jsx-max-props-per-line */
import React, { FC } from 'react';
import { Route, Switch, Redirect } from 'react-router-dom';
import * as routes from 'constants/constants.routes';
import { ContractTypes } from 'constants/constants.app';

import Tariffs from './Tariffs';

export const Router: FC = (): JSX.Element => {
  return (
    <Switch>
      <Route path={`${routes.CUSTOMERS_TARIFFS}/:tariffType/:type/:routeId/:transportType`} component={Tariffs} />
      <Route path={`${routes.CUSTOMERS_TARIFFS}/:tariffType/:type/:routeId`} component={Tariffs} />

      <Route exact path={routes.CUSTOMERS_TARIFFS} component={() => <Redirect to={`${routes.CUSTOMERS_TARIFFS}/${ContractTypes.INCOME}/passengers/all`} />} />
    </Switch>
  );
};

export default Router;

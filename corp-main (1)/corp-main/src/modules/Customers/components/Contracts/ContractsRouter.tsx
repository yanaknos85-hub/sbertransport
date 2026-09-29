/* eslint-disable @stylistic/jsx-max-props-per-line */
import React, { FC } from 'react';
import { Route, Switch, Redirect } from 'react-router-dom';
import * as routes from 'constants/constants.routes';
import { ContractTypes } from 'constants/constants.app';

import Contracts from './Contracts';

export const Router: FC = (): JSX.Element => {
  return (
    <Switch>
      <Route path={`${routes.CUSTOMERS_CONTRACTS}/:contractType/:type/:routeId`} component={Contracts} />

      <Route exact path={routes.CUSTOMERS_CONTRACTS} component={() => <Redirect to={`${routes.CUSTOMERS_CONTRACTS}/${ContractTypes.INCOME}/passengers/all`} />} />
    </Switch>
  );
};

export default Router;

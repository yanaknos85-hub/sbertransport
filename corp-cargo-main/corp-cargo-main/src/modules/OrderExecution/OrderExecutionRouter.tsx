/* eslint-disable @stylistic/jsx-max-props-per-line */
import React, { FC } from 'react';
import { Route, Switch, Redirect } from 'react-router-dom';
import { observer } from 'mobx-react';
import * as routes from 'constants/constants.routes';
import OrdersExecution from './OrderExecutionContent';
import CargoOrderDetailed from './components/OrderDetailed/Cargo';

export const Router: FC = observer((): JSX.Element => {
  return (
    <Switch>
      <Route path={`${routes.ORDER_EXECUTION_CARGO}/:source/:id`} component={CargoOrderDetailed} exact />
      <Route exact path={routes.ORDER_EXECUTION_CARGO} component={() => <Redirect to={`${routes.ORDER_EXECUTION_CARGO}/all`} />} />
    </Switch>
  );
});

export const devRouter: FC = observer((): JSX.Element => {
  return (
    <Switch>
      <Route path={`${routes.ORDER_EXECUTION}/:service/:category`} component={OrdersExecution} exact />
      <Route exact path={routes.ORDER_EXECUTION} component={() => <Redirect to={routes.ORDER_EXECUTION_CARGO} />} />
      <Router />
    </Switch>
  );
});

export default Router;

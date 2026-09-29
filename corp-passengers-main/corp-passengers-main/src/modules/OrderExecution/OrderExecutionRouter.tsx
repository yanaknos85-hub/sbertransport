/* eslint-disable @stylistic/max-len */
/* eslint-disable @stylistic/jsx-max-props-per-line */
import React, { FC } from 'react';
import { Route, Switch, Redirect } from 'react-router-dom';
import { observer } from 'mobx-react';
import * as routes from 'constants/constants.routes';

import OrdersExecution from './OrderExecutionContent';
import PassengersDetailed from './components/OrderDetailed/Passengers';

export const Router: FC = observer((): JSX.Element => {
  return (
    <>
      <Route path={`${routes.ORDER_EXECUTION_PASSENGERS}/taxi/:id`} component={PassengersDetailed} exact />
      <Route path={`${routes.ORDER_EXECUTION_PASSENGERS}/carsharing/:id`} component={PassengersDetailed} exact />
      <Route path={`${routes.ORDER_EXECUTION_PASSENGERS}/personal/:id`} component={PassengersDetailed} exact />
      <Route path={`${routes.ORDER_EXECUTION_PASSENGERS}/public/:id`} component={PassengersDetailed} exact />
      <Route path={`${routes.ORDER_EXECUTION_PASSENGERS}/group_transfer/:id`} component={PassengersDetailed} exact />
      <Route path={`${routes.ORDER_EXECUTION_PASSENGERS}/bus/:id`} component={PassengersDetailed} exact />
      {/* <Route path={`${routes.ORDER_EXECUTION_PASSENGERS}/complaints/:id`} component={PassengersDetailed} exact /> */}
      <Route exact path={routes.ORDER_EXECUTION_PASSENGERS} component={() => <Redirect to={`${routes.ORDER_EXECUTION_PASSENGERS}/taxi`} />} />
    </>
  );
});

export const devRouter: FC = observer((): JSX.Element => {
  return (
    <Switch>
      <Route path={`${routes.ORDER_EXECUTION}/:service/:category`} component={OrdersExecution} exact />
      <Route exact path={routes.ORDER_EXECUTION} component={() => <Redirect to={routes.ORDER_EXECUTION_PASSENGERS} />} />
      <Router />
    </Switch>
  );
});

export default Router;

/* eslint-disable @stylistic/max-len */
/* eslint-disable @stylistic/jsx-max-props-per-line */
import React, { FC } from 'react';
import { Route, Switch, Redirect } from 'react-router-dom';
import { observer } from 'mobx-react';
import mfLoader from 'mf/MFLoader';
import * as routes from 'constants/constants.routes';

import OrdersExecution from './OrderExecution';

const OrderExecutionPassengersRouter = React.lazy(() => mfLoader(import('passengers/modules/OrderExecution/Router')));
const OrderExecutionCargoRouter = React.lazy(() => mfLoader(import('cargo/modules/OrderExecution/Router')));
const OrderExecutionFleetRouter = React.lazy(() => mfLoader(import('fleet/modules/OrderExecution/Router')));

export const Router: FC = observer((): JSX.Element => {
  return (
    <Switch>
      <Route path={`${routes.ORDER_EXECUTION}/:service/:category`} component={OrdersExecution} exact />

      <Route path={`${routes.ORDER_EXECUTION_PASSENGERS}`} component={OrderExecutionPassengersRouter} />
      <Route path={`${routes.ORDER_EXECUTION_CARGO}`} component={OrderExecutionCargoRouter} />
      <Route path={`${routes.ORDER_EXECUTION_CAR_SERVICE}`} component={OrderExecutionFleetRouter} />

      <Route exact path={routes.ORDER_EXECUTION} component={() => <Redirect to={routes.ORDER_EXECUTION_PASSENGERS} />} />
    </Switch>
  );
});

export default Router;

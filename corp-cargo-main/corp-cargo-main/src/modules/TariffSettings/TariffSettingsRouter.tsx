/* eslint-disable @stylistic/jsx-max-props-per-line */
import React, { FC, lazy } from 'react';
import { Route, Switch, Redirect } from 'react-router-dom';
import * as routes from 'constants/constants.routes';

const Contracts = lazy(() => import('./components/Contracts'));
const Tariffs = lazy(() => import('./components/Tariffs'));

export const Router: FC = (): JSX.Element => {
  return (
    <Switch>
      <Route path={`${routes.CONTRACTORS}/:routeId`} component={() => (<>Не предусмотрено для Грузов</>)} />
      <Route path={`${routes.CONTRACTS}/:type/:routeId`} component={Contracts} />
      <Route path={`${routes.TARIFFS}/:type/:routeId/:transportType`} component={Tariffs} />
      <Route path={`${routes.TARIFFS}/:type/:routeId`} component={Tariffs} />
      <Route exact path={routes.TARIFF_SETTINGS} component={() => <Redirect to={`${routes.CONTRACTORS}/all`} />} />
      <Route exact path={routes.CONTRACTS} component={() => <Redirect to={`${routes.CONTRACTS}/cargo/all`} />} />
      <Route exact path={routes.TARIFFS} component={() => <Redirect to={`${routes.TARIFFS}/cargo/all`} />} />
      <Route path="*" component={() => <Redirect to={routes.TARIFF_SETTINGS} />} />
    </Switch>
  );
};

export default Router;
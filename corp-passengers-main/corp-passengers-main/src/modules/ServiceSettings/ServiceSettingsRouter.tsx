/* eslint-disable @stylistic/jsx-max-props-per-line */
import React, { FC } from 'react';
import { Route, Switch, Redirect } from 'react-router-dom';
import * as routes from 'constants/constants.routes';

import NotificationsSettings from './components/NotificationsSettings/NotificationsSettings';
import RegistrySettings from './components/RegistrySettings/RegistrySettings';

export const Router: FC = (): JSX.Element => {
  return (
    <Switch>
      {/* TODO убрать если не нужен <Route path={routes.DEADLINES} component={DeadlineSettings} /> */}
      <Route path={routes.NOTIFICATIONS} component={NotificationsSettings} />
      <Route path={routes.REFERENCE_BOOKS} component={RegistrySettings} />

      <Route exact path={routes.SERVICE_SETTINGS} component={() => <Redirect to={`${routes.NOTIFICATIONS}`} />} />
      <Route path="*" component={() => <Redirect to={routes.SERVICE_SETTINGS} />} />
    </Switch>
  );
};

export default Router;

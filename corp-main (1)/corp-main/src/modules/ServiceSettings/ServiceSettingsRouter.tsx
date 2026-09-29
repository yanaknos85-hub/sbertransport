/* eslint-disable @stylistic/jsx-max-props-per-line */
import React, { FC } from 'react';
import { Route, Switch, Redirect } from 'react-router-dom';
import { observer } from 'mobx-react';
import * as routes from 'constants/constants.routes';

import { DeadlineSettings } from './components/DeadlineSettings/DeadlineSettings';
import NotificationsSettings from './components/NotificationsSettings/NotificationsSettings';
import RegistrySettings from './components/RegistrySettings/RegistrySettings';

export const Router: FC = observer((): JSX.Element => {
  return (
    <Switch>
      <Route path={routes.DEADLINES} component={DeadlineSettings} />
      <Route path={routes.NOTIFICATIONS} component={NotificationsSettings} />
      <Route path={`${routes.REFERENCE_BOOKS}/:type`} component={RegistrySettings} />

      <Route exact path={routes.REFERENCE_BOOKS} component={() => <Redirect to={`${routes.REFERENCE_BOOKS}/common`} />} />
      <Route exact path={routes.SERVICE_SETTINGS} component={() => <Redirect to={`${routes.NOTIFICATIONS}`} />} />
      <Route path="*" component={() => <Redirect to={routes.SERVICE_SETTINGS} />} />
    </Switch>
  );
});

export default Router;

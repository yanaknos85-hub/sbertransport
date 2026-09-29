/* eslint-disable @stylistic/max-len */
/* eslint-disable @stylistic/jsx-max-props-per-line */
import React, { FC } from 'react';
import { Route, Switch } from 'react-router-dom';
import { observer } from 'mobx-react';
import * as routes from 'constants/constants.routes';

import ExecutorGroupContent from './ExecutorGroupContent';

export const Router: FC = observer((): JSX.Element => {
  return (
    <>
      <Route path={routes.EXECUTOR_GROUPS} component={ExecutorGroupContent} exact />
    </>
  );
});

export const devRouter: FC = observer((): JSX.Element => {
  return (
    <Switch>
      <Router />
    </Switch>
  );
});

export default Router;

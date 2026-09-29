import React, { FC } from 'react';

import { Route, Switch, Redirect } from 'react-router-dom';

import * as routes from 'constants/routes.constants';

import { SchedulerTabs } from './constants/schedule.constants';
import Schedule from './Schedule';

export const Router: FC = (): JSX.Element => {
  return (
    <Switch>
      <Route path={`${routes.SCHEDULE}/:tab/:routeId?`} component={Schedule} />

      <Route path="*" component={() => <Redirect to={`${routes.SCHEDULE}/${SchedulerTabs.Shift}`} />} />
    </Switch>
  );
};

export default Router;

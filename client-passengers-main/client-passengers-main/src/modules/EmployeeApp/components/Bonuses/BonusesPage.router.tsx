import React from 'react';
import type { FC } from 'react';
import { Route, Switch } from 'react-router-dom';
import * as routes from 'constants/constants.routes';

import { AccountPage } from './AccountPage';

export const BonusesPageRouter: FC = () => (
  <Switch>
    <Route
      path={routes.BONUSES_ACCOUNT_PAGE}
      component={AccountPage}
      exact={true}
    />
  </Switch>
);

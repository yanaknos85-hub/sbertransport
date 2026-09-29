import React, { FC } from 'react';
import { Route, Switch, useRouteMatch } from 'react-router-dom';

import { AccountPage } from './AccountPage';

export const BonusesPageRouter: FC = () => {
  const match = useRouteMatch();

  return (
    <Switch>
      <Route
        path={`${match.path}/account`}
        component={AccountPage}
        exact={true}
      />
    </Switch>
  );
};

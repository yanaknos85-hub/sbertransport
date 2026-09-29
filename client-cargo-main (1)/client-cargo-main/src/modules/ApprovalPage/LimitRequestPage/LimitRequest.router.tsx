import React, { FC, ReactNode } from 'react';
import {
  Redirect, Route, Switch, useRouteMatch
} from 'react-router-dom';

import LimitRequestDetailedView from './LimitRequestDetailedView/LimitRequestDetailedView';
import LimitRequestList from './LimitRequestList/LimitRequestList';

const LimitRequestRouter: FC = () => {
  const match = useRouteMatch();

  return (
    <Switch>
      <Route
        path={`${match.path}/:filter`}
        component={LimitRequestList}
        exact={true}
      />
      <Route
        path={`${match.path}/:filter/:reqId`}
        component={LimitRequestDetailedView}
        exact={true}
      />
      <Route render={(): ReactNode => <Redirect to={`${match.path}/active`} />} />
    </Switch>
  );
};

export default LimitRequestRouter;

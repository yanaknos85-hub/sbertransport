import React, { FC, ReactNode } from 'react';
import { Redirect, Route, Switch } from 'react-router-dom';

import { EXCHANGE } from 'constants/constants.routes';

import Exchange from '../Exchange/Exchange';
import { ExchangeDetailed } from './components/Detailed/Detailed';

const ExchangeRouter: FC = () => {
  return (
    <Switch>
      <Route
        path={`${EXCHANGE}/:type`}
        component={Exchange}
        exact={true}
      />
      <Route
        path={`${EXCHANGE}/:type/:id`}
        component={ExchangeDetailed}
        exact={true}
      />
      <Route render={(): ReactNode => <Redirect to={`${EXCHANGE}/available`} exact={true} />} />
    </Switch>
  );
};

export default ExchangeRouter;

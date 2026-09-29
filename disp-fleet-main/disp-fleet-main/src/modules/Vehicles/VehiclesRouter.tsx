import React, { FC } from 'react';
import { Route, Switch, Redirect } from 'react-router-dom';

import * as routes from 'constants/routes.constants';

import Vehicles from './Vehicles';

export const Router: FC = (): JSX.Element => {
  return (
    <Switch>
      <Route path={`${routes.VEHICLES}/:routeId?`} component={Vehicles} />

      <Route path="*" component={() => <Redirect to={routes.VEHICLES} />} />
    </Switch>
  );
};

export default Router;

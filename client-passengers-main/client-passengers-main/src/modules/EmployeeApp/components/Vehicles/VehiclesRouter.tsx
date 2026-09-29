import React, { FC } from 'react';
import { Route, Switch, useRouteMatch } from 'react-router-dom';

import { VehicleListAsPage } from './components/VehiclesList/VehiclesList';
import VehicleView from './components/VehiclesView/VehiclesView';

const VehiclesRouter: FC = () => {
  const match = useRouteMatch();
  return (
    <Switch>
      <Route
        exact={true}
        path={`${match.path}`}
        component={VehicleListAsPage}
      />
      <Route
        exact={true}
        path={`${match.path}/add`}
        component={VehicleView}
      />
      <Route path={`${match.path}/:id`} component={VehicleView} />
    </Switch>
  );
};

export default VehiclesRouter;

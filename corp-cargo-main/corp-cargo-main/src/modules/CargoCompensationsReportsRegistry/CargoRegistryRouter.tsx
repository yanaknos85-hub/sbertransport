import { ToolbarProvider } from 'components/Toolbar';
import React, { FC } from 'react';
import { Route, Switch } from 'react-router-dom';
import TasksTab from './components/Table';
import * as routes from 'constants/constants.routes';

export const Router: FC = () => {
  return (
    <Switch>
      <ToolbarProvider>
        <Route
          path={routes.REGISTRY_CARGO_COMPENSATIONS_REPORTS}
          render={() => <TasksTab />}
          exact
        />
      </ToolbarProvider>
    </Switch>
  );
};

export default Router;

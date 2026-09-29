import { ToolbarProvider } from 'components/Toolbar';
import React, { FC } from 'react';
import {
  Route, Switch, useLocation
} from 'react-router-dom';
import { ListView } from './components/ListView';
import * as routes from 'constants/constants.routes';

export const Router: FC = () => {
  const { pathname } = useLocation();

  return (
    <Switch>
      <ToolbarProvider>
        <Route
          path={routes.REGISTRY_CARGO_COMPENSATIONS}
          render={() => <ListView pathname={pathname} />}
          exact
        />
      </ToolbarProvider>
    </Switch>
  );
};

export default Router;

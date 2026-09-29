import { ToolbarProvider } from 'components/Toolbar';
import React, { FC } from 'react';
import {
  Route, Switch, useLocation, useParams
} from 'react-router-dom';
import { DetailedView } from './components/DetailedView';
import { ListView } from './components/ListView';
import * as routes from 'constants/constants.routes';

export const Router: FC = () => {
  const { id } = useParams<{ id?: string }>();
  const { pathname } = useLocation();

  return (
    <Switch>
      <ToolbarProvider>
        <Route
          path={routes.REGISTRY_CARGO_ORDERS}
          render={() => <ListView pathname={pathname} />}
          exact
        />

        <Route
          path={routes.REGISTRY_CARGO_ORDERS_VIEW}
          component={() => <DetailedView id={id as string} />}
          exact
        />
      </ToolbarProvider>
    </Switch>
  );
};

export default Router;

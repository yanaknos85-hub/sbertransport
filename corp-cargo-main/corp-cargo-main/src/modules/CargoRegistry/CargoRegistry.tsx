import { ToolbarProvider } from 'components/Toolbar';
import React, { FC } from 'react';
import {
  Route, Switch, useLocation, useParams
} from 'react-router-dom';
import { DetailedView } from './components/DetailedView';
import { ListView } from './components/ListView';

export const CargoRegistry: FC = () => {
  const { id } = useParams<{ id?: string }>();
  const { pathname } = useLocation();

  return (
    <Switch>
      <ToolbarProvider>
        <Route
          path="/reports/registry/cargo"
          render={() => <ListView pathname={pathname} />}
          exact
        />

        <Route
          path="/reports/registry/cargo/:id"
          component={() => <DetailedView id={id as string} />}
          exact
        />
      </ToolbarProvider>
    </Switch>
  );
};

import { observer } from 'mobx-react';
import React, { FC } from 'react';
import {
  Redirect, Route, Switch, useRouteMatch
} from 'react-router-dom';
import ApprovalYandexList from './components/ApprovalYandexList/ApprovalYandexList';
import ApprovalYandexDetailedView from './components/ApprovalYandexDetailedView/ApprovalYandexDetailedView';

export const ApprovalYandexRouter: FC = observer(() => {
  const { url: path } = useRouteMatch();

  return (
    <Switch>
      <Route
        path={`${path}/:filter`}
        component={ApprovalYandexList}
        exact
      />
      <Route
        path={`${path}/:filter/:id`}
        component={ApprovalYandexDetailedView}
        exact
      />

      <Redirect from={path} to={`${path}/active`} />
    </Switch>
  );
});

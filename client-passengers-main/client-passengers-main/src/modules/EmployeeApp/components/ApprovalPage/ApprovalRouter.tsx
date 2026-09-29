import React from 'react';
import type { FC } from 'react';
import { Redirect, Route, Switch } from 'react-router-dom';
import * as routes from 'constants/constants.routes';

import ApprovalRequestsRouter from './ApprovalRequestsRouter';
import DelegateCard from './DelegatesPage/DelegateCard';
import DelegatesList from './DelegatesPage/DelegatesList';
import NewDelegateCard from './DelegatesPage/NewDelegateCard';
import { ApprovalYandexRouter } from './ApprovalYandexTaxi/ApprovalYandexRouter';

const ApprovalRouter: FC = () => (
  <Switch>
    <Route path={routes.APPROVEMENT_TRIPS} component={ApprovalRequestsRouter} />

    <Route
      path={routes.APPROVEMENT_DELEGATES}
      component={DelegatesList}
      exact
    />
    <Route
      path={routes.APPROVEMENT_DELEGATE_CREATE}
      component={NewDelegateCard}
      exact
    />
    <Route
      path={routes.APPROVEMENT_DELEGATE}
      component={DelegateCard}
      exact
    />

    <Route
      path={routes.APPROVEMENT_YANDEX}
      component={ApprovalYandexRouter}
    />

    <Route render={() => <Redirect to={routes.APPROVEMENT_TRIPS} />} />
  </Switch>
);

export default ApprovalRouter;


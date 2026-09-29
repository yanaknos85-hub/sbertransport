import React, { FC, useEffect } from 'react';
import { Route, Switch, useRouteMatch } from 'react-router-dom';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { StoreNames } from 'stores/StoreNames.enum';
import { EmployeeAppLinks } from 'constants/EmployeeApp.constants';

import RequestsList from './Components/limitRequests/UserLimitRequestList';
import UserLimitRequestDetailedView from './Components/UserLimitRequestDetailedView/UserLimitRequestDetailedView';
import { LimitsPage } from './LimitsPage';

export const LimitsPageRouter: FC = () => {
  const match = useRouteMatch();
  const { [StoreNames.limitsStore]: limitsStore } = useAppStoreContext();

  useEffect(() => {
    limitsStore.initStore();
  }, [limitsStore]);

  return (
    <Switch>
      <Route
        path={`${match.path}`}
        component={LimitsPage}
        exact={true}
      />
      <Route path={`${match.path}/${EmployeeAppLinks.limitsInfo}`} component={LimitsPage} />
      <Route
        path={`${match.path}/${EmployeeAppLinks.limitRequests}/:filter`}
        component={RequestsList}
        exact={true}
      />
      <Route
        path={`${match.path}/${EmployeeAppLinks.limitRequests}/:filter/:reqId`}
        component={UserLimitRequestDetailedView}
        exact={true}
      />
      {/* <Route render={(): ReactNode => <Redirect to={`${match.path}/${EmployeeAppLinks.limitRequests}/active`} />} /> */}
    </Switch>
  );
};

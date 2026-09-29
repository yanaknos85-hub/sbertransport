import React, { FC } from 'react';
import { Route, Switch, useRouteMatch } from 'react-router-dom';
import { observer } from 'mobx-react';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores';
import ApprovalRequestsRouter from './ApprovalRequests.router';
import LimitRequestRouter from './LimitRequest.router';
import DelegatesRouter from './Delegates.router';

const ApprovalRouter: FC = observer(() => {
  const match = useRouteMatch();
  const { [StoreNames.selfStore]: { selfEmployee } } = useAppStoreContext();
  const { isDepartmentHead } = selfEmployee;

  return (
    <Switch>
      <Route path={`${match.path}/requests`} component={ApprovalRequestsRouter} />
      <Route path={`${match.path}/limits`} component={LimitRequestRouter} />
      {isDepartmentHead && <Route path={`${match.path}/delegates`} component={DelegatesRouter} />}
    </Switch>
  );
});

export default ApprovalRouter;

import React, { FC } from 'react';
import {
  Redirect, Route, Switch, useRouteMatch
} from 'react-router-dom';
import { observer } from 'mobx-react';
import { ApprovalTypeEnum } from 'shared/models/Approval.interface';

import ApprovalDetailedRequestView from './ApprovalDetailedView/ApprovalDetailedRequestView';
import ApprovalDetailedSharedRideView from './ApprovalDetailedView/ApprovalDetailedSharedRideView';
import ApprovalDetailedTripView from './ApprovalDetailedView/ApprovalDetailedTripView';
import ApprovalDetailedUpdatedTripView from './ApprovalDetailedView/ApprovalDetailedUpdatedTripView';
import ApprovalList from './ApprovalList';

// Get correct approval type
const getType = (type: keyof typeof ApprovalTypeEnum) => ApprovalTypeEnum[type].toLowerCase();

const ApprovalRequestsRouter: FC = observer(() => {
  const { url } = useRouteMatch();

  return (
    <Switch>
      <Route
        path={`${url}/:filter`}
        component={ApprovalList}
        exact={true}
      />
      <Route
        path={`${url}/:filter/${getType('REQUEST')}/:reqId/:approvalId`}
        component={ApprovalDetailedRequestView}
        exact={true}
      />
      <Route
        path={`${url}/:filter/${getType('TRIP')}/:reqId/:approvalId`}
        component={ApprovalDetailedTripView}
        exact={true}
      />
      <Route
        path={`${url}/:filter/${getType('SHARED_RIDE')}/:reqId/:approvalId`}
        component={ApprovalDetailedSharedRideView}
        exact={true}
      />
      <Route
        path={`${url}/:filter/${getType('UPDATED_TRIP')}/:reqId`}
        component={ApprovalDetailedUpdatedTripView}
        exact={true}
      />
      <Redirect from={url} to={`${url}/active`} />
    </Switch>
  );
});

export default ApprovalRequestsRouter;

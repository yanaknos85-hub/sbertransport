import { observer } from 'mobx-react';
import React, { FC } from 'react';
import {
  Redirect, Route, Switch, useRouteMatch
} from 'react-router-dom';

import { ApprovalTypeEnum } from 'shared/models/Approval.interface';

// import { EditTripRequestComponent } from '../EditTripRequest/EditTripRequest.component';
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
        exact
      />
      <Route
        path={`${url}/:filter/${getType('REQUEST')}/:reqId/:approvalId`}
        component={ApprovalDetailedRequestView}
        exact
      />
      <Route
        path={`${url}/:filter/${getType('TRIP')}/:reqId/:approvalId`}
        component={ApprovalDetailedTripView}
        exact
      />
      <Route
        path={`${url}/:filter/${getType('SHARED_RIDE')}/:reqId/:approvalId`}
        component={ApprovalDetailedSharedRideView}
        exact
      />
      <Route
        path={`${url}/:filter/${getType('UPDATED_TRIP')}/:reqId`}
        component={ApprovalDetailedUpdatedTripView}
        exact
      />
      {/* FIXME отключено в рамках TRANSPORT-3872 */}
      {/* <Route
        path={`${url}/:filter/${getType('TRIP')}/:reqId/:approvalId/edit`}
        component={EditTripRequestComponent}
        exact
      /> */}
      {/* <Route
        path={`${url}/:filter/${getType('SHARED_RIDE')}/:reqId/:approvalId/edit`}
        component={EditTripRequestComponent}
        exact
      /> */}
      {/* <Route
        path={`${url}/:filter/${getType('REQUEST')}/:reqId/:approvalId/edit`}
        component={EditTripRequestComponent}
        exact
      /> */}
      <Redirect from={url} to={`${url}/active`} />
    </Switch>
  );
});

export default ApprovalRequestsRouter;

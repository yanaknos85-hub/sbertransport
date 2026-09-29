import { observer } from 'mobx-react';
import React, { FC, useEffect } from 'react';
import {
  Redirect, Route, Switch, useRouteMatch
} from 'react-router-dom';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores/StoreNames.enum';
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

  const { [StoreNames.corporateStore]: corporateStore } = useAppStoreContext();

  useEffect(() => {
    corporateStore.initStore();
  }, [corporateStore]);

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

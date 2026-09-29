import React, { FC, useCallback, useEffect } from 'react';
import { useHistory, useRouteMatch } from 'react-router-dom';

import EmptyItem from 'modules/EmployeeApp/shared/EmptyFactory';

import { SpinWrapped } from 'shared/components';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import { useCurrentTripRequest } from 'shared/hooks/trip/useCurrentTripRequest';

import Content from './Content';
import { APPROVEMENT_TRIPS } from 'constants/constants.routes';
import { StoreNames, useAppStore } from 'stores';

const ApprovalDetailedRequestView: FC<any> = () => {
  const { [StoreNames.approvalsStore]: approvalsStore } = useAppStore();
  const history = useHistory();
  const { currentTripRequest, inProgress: requestInProgress } = useCurrentTripRequest();
  const { params } = useRouteMatch<{ filter: 'active' | 'closed' }>();

  const goToPlanned = useCallback(() => {
    history.push(`${APPROVEMENT_TRIPS}/${params?.filter || ''}`);
  }, [history]);

  useEffect(() => {
    return () => {
      approvalsStore.setDetailedPageStatus(true);
    };
  }, []);

  if (!currentTripRequest && !requestInProgress) {
    return (
      <EmptyItem
        back={goToPlanned}
        message="Заявка с таким ID отсутствует в списке для согласования"
        action="Вернуться к списку заявок"
      />
    );
  }
  return requestInProgress ? <SpinWrapped /> : <Content request={currentTripRequest} back={goToPlanned} />;
};

export default withErrorBoundary(ApprovalDetailedRequestView);

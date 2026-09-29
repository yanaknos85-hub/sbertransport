import React, { FC, useCallback, useEffect } from 'react';
import { useHistory, useRouteMatch } from 'react-router-dom';

import EmptyItem from 'modules/EmployeeApp/shared/EmptyFactory';

import { StoreNames, useAppStore } from 'stores';
import { SpinWrapped } from 'shared/components';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import { useCurrentTripRequest } from 'shared/hooks/trip/useCurrentTripRequest';

import Content from './Content';
import { APPROVEMENT_TRIPS } from 'constants/constants.routes';

const ApprovalDetailedTripView: FC = () => {
  const { [StoreNames.approvalsStore]: approvalsStore } = useAppStore();
  const history = useHistory();
  const { params } = useRouteMatch<{ filter: 'active' | 'closed' }>();

  const { currentTripRequest, inProgress: requestInProgress } = useCurrentTripRequest();

  const goBack = useCallback(() => {
    history.push(`${APPROVEMENT_TRIPS}/${params.filter || ''}`);
  }, [history]);

  useEffect(() => {
    return () => {
      approvalsStore.setDetailedPageStatus(true);
    };
  }, []);

  if (!currentTripRequest && !requestInProgress) {
    return (
      <EmptyItem
        back={goBack}
        message="Заявка с таким ID отсутствует в списке для согласования"
        action="Вернуться к списку заявок на поездки"
      />
    );
  }
  return requestInProgress ? <SpinWrapped /> : <Content request={currentTripRequest} back={goBack} />;
};

export default withErrorBoundary(ApprovalDetailedTripView);

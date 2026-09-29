import React, { FC, useCallback, useEffect } from 'react';
import { useHistory, useRouteMatch } from 'react-router-dom';

import EmptyItem from 'modules/EmployeeApp/shared/EmptyFactory';

import { SpinWrapped } from 'shared/components';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import { useCurrentTripRequest } from 'shared/hooks/trip/useCurrentTripRequest';

import Content from './Content';
import { useUpdatedTripRequest } from './hooks/useUpdatedTripRequest';
import { APPROVEMENT_TRIPS } from 'constants/constants.routes';
import { StoreNames, useAppStore } from 'stores';

const ApprovalDetailedUpdatedTripView: FC = () => {
  const { [StoreNames.approvalsStore]: approvalsStore } = useAppStore();
  const history = useHistory();
  const { params } = useRouteMatch<{ filter: 'active' | 'closed' }>();

  const { currentTripRequest, inProgress: requestInProgress } = useCurrentTripRequest();
  const { tripRequest: updatedRequest, inProgress: updatedRequestInProgress } = useUpdatedTripRequest();

  const goBack = useCallback(() => {
    history.push(`${APPROVEMENT_TRIPS}/${params?.filter || ''}`);
  }, [history]);

  const inProgress = requestInProgress || updatedRequestInProgress;

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

  return inProgress ? (
    <SpinWrapped />
  ) : (
    // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
    <Content
      request={currentTripRequest!}
      updatedRequest={updatedRequest}
      back={goBack}
    />
  );
  // FIXME @typescript-eslint/no-non-null-assertion
};

export default withErrorBoundary(ApprovalDetailedUpdatedTripView);

import React, { FC, useCallback } from 'react';
import { useHistory } from 'react-router-dom';

import EmptyItem from 'shared/components/EmptyFactory/EmptyFactory';

import { SpinWrapped } from 'shared/components';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import { useCurrentTripRequest } from 'shared/hooks/trip/useCurrentTripRequest';

import Content from './Content';

const ApprovalDetailedRequestView: FC<any> = () => {
  const history = useHistory();
  const { currentTripRequest, inProgress: requestInProgress } = useCurrentTripRequest();

  const goToPlanned = useCallback(() => {
    history.push('../');
  }, [history]);

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

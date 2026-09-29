import React, { FC, useCallback } from 'react';
import { useHistory } from 'react-router-dom';

import EmptyItem from 'shared/components/EmptyFactory/EmptyFactory';

import { SpinWrapped } from 'shared/components';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import { useCurrentTripRequest } from 'shared/hooks/trip/useCurrentTripRequest';

import Content from './Content';

const ApprovalDetailedTripView: FC = () => {
  const history = useHistory();

  const { currentTripRequest, inProgress: requestInProgress } = useCurrentTripRequest();

  const goBack = useCallback(() => {
    history.push('../');
  }, [history]);

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

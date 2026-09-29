import React, { FC, useCallback } from 'react';
import { useHistory as History } from '@sber-sbertransport/mf-core';
import { SpinWrapped } from 'shared/components';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import EmptyItem from 'shared/EmptyFactory';
import { useCurrentTripRequest } from 'shared/hooks/trip/useCurrentTripRequest';

import Content from './Content';

const ApprovalDetailedTripView: FC = () => {
  const history = History();

  const { currentTripRequest, inProgress: requestInProgress } = useCurrentTripRequest();

  const goBack = useCallback(() => {
    history.push('./');
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

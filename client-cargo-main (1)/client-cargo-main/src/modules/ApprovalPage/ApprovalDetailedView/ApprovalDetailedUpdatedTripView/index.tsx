import React, { FC, useCallback } from 'react';
import { useHistory as History } from '@sber-sbertransport/mf-core';
import { SpinWrapped } from 'shared/components';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import EmptyItem from 'shared/EmptyFactory';
import { useCurrentTripRequest } from 'shared/hooks/trip/useCurrentTripRequest';

import Content from './Content';
import { useUpdatedTripRequest } from './hooks/useUpdatedTripRequest';

const ApprovalDetailedUpdatedTripView: FC = () => {
  const history = History();

  const { currentTripRequest, inProgress: requestInProgress } = useCurrentTripRequest();
  const { tripRequest: updatedRequest, inProgress: updatedRequestInProgress } = useUpdatedTripRequest();

  const goBack = useCallback(() => {
    history.push('../');
  }, [history]);

  const inProgress = requestInProgress || updatedRequestInProgress;

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

import React, { FC, useCallback } from 'react';
import { useHistory } from 'react-router-dom';

import EmptyItem from 'shared/components/EmptyFactory/EmptyFactory';

import { SpinWrapped } from 'shared/components';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import { useCurrentTripRequest } from 'shared/hooks/trip/useCurrentTripRequest';

import Content from './Content';
import { useUpdatedTripRequest } from './hooks/useUpdatedTripRequest';

const ApprovalDetailedUpdatedTripView: FC = () => {
  const history = useHistory();

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

import { observer } from 'mobx-react';
import React, { useState } from 'react';
import { useRouteMatch } from 'react-router-dom';

import { useFinalTripApprove, useFinalTripDecline } from 'api/approvals';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import { TripRequestModel } from 'stores/Trip/models';
import { IDeclineReason } from 'stores/Trip/Trip.interface';

import ApprovalReasonModal from '../../ApprovalReasonModal/ApprovalReasonModal';

import { ApprovalView } from './ui/ApprovalView/ApprovalView';

const Content = observer(({ request, back }: { request: TripRequestModel; back(): void }) => {
  const match = useRouteMatch<{ approvalId: string; filter: 'active' | 'closed' }>();
  const { approvalId } = match.params;

  const [finalTripApprove] = useFinalTripApprove();
  const [finalTripDecline] = useFinalTripDecline();

  const [reasonVisible, setReasonVisible] = useState(false);
  const toggleReasonModal = (): void => setReasonVisible(x => !x);

  // eslint-disable-next-line react-hooks/exhaustive-deps
  // const editHandler = useCallback(() => history.push(`${url}/edit`), [history]);
  // FIXME react-hooks/exhaustive-deps

  const handleApprove = (): void => {
    finalTripApprove({ id: approvalId }).then(() => {
      back();
    });
  };

  return (
    <>
      <ApprovalReasonModal
        id={approvalId}
        visible={reasonVisible}
        onOk={(id, reason: IDeclineReason): void => {
          finalTripDecline({ id, reason }).then(() => {
            toggleReasonModal();
            back();
          });
        }}
        onCancel={toggleReasonModal}
      />
      <ApprovalView
        onApprove={handleApprove}
        onCancel={toggleReasonModal}
        request={request}
        back={back}
      />
    </>
  );
});

export default withErrorBoundary(Content);

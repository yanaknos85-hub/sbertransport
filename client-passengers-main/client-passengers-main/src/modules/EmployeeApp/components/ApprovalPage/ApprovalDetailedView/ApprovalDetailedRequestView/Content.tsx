import React, { FC, useState } from 'react';
import { observer } from 'mobx-react';
import { useRouteMatch } from 'react-router-dom';

import { useRequestApprove, useRequestDecline } from 'api/approvals';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import { TripRequestModel } from 'stores/Trip/models';
import { IDeclineReason } from 'stores/Trip/Trip.interface';

import ApprovalReasonModal from '../../ApprovalReasonModal/ApprovalReasonModal';
import { ApprovalView } from '../ApprovalDetailedTripView/ui/ApprovalView/ApprovalView';

const Content: FC<any> = observer(({ back, request }: { back(): void; request: TripRequestModel }) => {
  const match = useRouteMatch<{ approvalId: string; filter: 'active' | 'closed' }>();
  const { approvalId } = match.params;

  const [requestApprove] = useRequestApprove();
  const [requestDecline] = useRequestDecline();

  const [reasonVisible, setReasonVisible] = useState(false);
  const toggleReasonModal = (): void => setReasonVisible(x => !x);

  function approve(id: string | undefined): void {
    if (id) {
      requestApprove({ id }).then(() => {
        back();
      });
    }
  }

  // eslint-disable-next-line react-hooks/exhaustive-deps
  // const editHandler = useCallback(() => history.push(`${url}/edit`), [history]);
  // FIXME react-hooks/exhaustive-deps

  // const isPersonalTransport = request.transportType === TransportTypeEnum.PERSONAL;

  const handleApprove = (): void => approve(approvalId);

  return (
    <>
      <ApprovalReasonModal
        id={approvalId}
        visible={reasonVisible}
        onOk={(id, reason: IDeclineReason): void => {
          requestDecline({ id, reason }).then(() => {
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

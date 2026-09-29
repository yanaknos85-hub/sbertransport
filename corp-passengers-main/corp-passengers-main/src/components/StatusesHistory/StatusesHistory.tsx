import type { FC } from 'react';
import React, { useMemo } from 'react';

import type { UUID } from 'utils/io-ts';
import { useGetTripRequestHistory } from 'api/history';
import {
  CanceledStatuses,
  FinishedStatuses,
  StatusesOrder,
  PersonalStatusesOrder,
  PersonalSharedStatusesOrder,
  StatusNames
} from 'constants/constants.statuses';
import HistoryModal, { IHistory } from 'shared/components/Statuses/HistoryModal';
import { TransportTypes } from 'stores/TransportTypes/TransportTypes.interface';

interface Props {
  visible: boolean;
  transportType: TransportTypes;
  tripId: UUID;
  isSharedRideSub: boolean | undefined;
  statusCodeDescription: string | undefined;
  approver: {
    id: UUID;
    fullName: string;
  } | null;
  onClose: () => void;
}

const StatusesHistory: FC<Props> = ({
  visible,
  transportType,
  tripId,
  isSharedRideSub,
  statusCodeDescription,
  approver,
  onClose,
}) => {
  const { data: actualHistory, isLoading } = useGetTripRequestHistory(tripId);

  const history = useMemo<IHistory>(() => {
    const targetStatusNames = StatusNames[transportType];
    let targetStatusesOrder;
    if (transportType === TransportTypes.PERSONAL) {
      targetStatusesOrder = isSharedRideSub ? PersonalSharedStatusesOrder : PersonalStatusesOrder;
    } else {
      targetStatusesOrder = StatusesOrder[transportType] ?? [];
    }

    const templateHistoryStatuses = targetStatusesOrder.map(status => ({
      date: null,
      status,
      statusName: targetStatusNames[status] ?? status,
    }));

    if (!actualHistory?.length) {
      return {
        canceled: false,
        finished: false,
        currentIndex: 0,
        historyStatuses: templateHistoryStatuses,
      };
    }

    const currentIndex = actualHistory.length - 1;
    const currentItem = actualHistory[currentIndex];
    const canceled = CanceledStatuses.includes(currentItem.status);
    let finished = false;
    if (!canceled) {
      finished = FinishedStatuses.includes(currentItem.status);
    }

    const currentHistoryStatuses = actualHistory.map(({ changeDate, status }) => ({
      date: changeDate,
      status,
      statusName: targetStatusNames[status] ?? status,
    }));

    const intersectionIndex = templateHistoryStatuses.findIndex(({ status }) => status === currentItem.status);

    const sliceIndex = finished || canceled || intersectionIndex < 0 ? templateHistoryStatuses.length
      : intersectionIndex + 1;

    const historyStatuses = [
      ...currentHistoryStatuses,
      ...templateHistoryStatuses.slice(sliceIndex),
    ];

    return {
      canceled,
      finished,
      currentIndex,
      historyStatuses,
    };
  }, [actualHistory]);

  return (
    <HistoryModal
      visible={visible}
      loading={isLoading}
      title="Детальный статус заявки"
      history={history}
      cancelDescription={statusCodeDescription}
      approver={approver}
      onClose={onClose}
    />
  );
};

export default StatusesHistory;

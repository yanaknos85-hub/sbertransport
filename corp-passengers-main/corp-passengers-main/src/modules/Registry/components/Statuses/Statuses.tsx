import React from 'react';
import type { FC } from 'react';
import { Button } from 'antd';
import { ClockCircleOutlined } from '@ant-design/icons';

import type { UUID } from 'utils/io-ts';
import { useModalState } from 'shared/hooks/useModal';
import { TransportTypes } from 'stores/TransportTypes/TransportTypes.interface';
import StatusesHistory from 'components/StatusesHistory/StatusesHistory';
import styles from './styles.module.scss';

interface Props {
  currentName: string;
  transportType: TransportTypes;
  isSharedRideSub?: boolean;
  tripId: UUID;
  statusCodeDescription: string | undefined;
  approver: {
    id: UUID;
    fullName: string;
  } | null;
}

const Statuses: FC<Props> = ({
  currentName,
  transportType,
  isSharedRideSub,
  tripId,
  statusCodeDescription,
  approver,
}) => {
  const [visible, { hide, show }] = useModalState();

  return (
    <>
      <Button
        type="link"
        className={styles.status}
        onClick={show}
      >
        <span className={styles.status__value}>{currentName}</span>
        <ClockCircleOutlined />
      </Button>

      {visible && (
        <StatusesHistory
          visible={visible}
          transportType={transportType}
          isSharedRideSub={isSharedRideSub}
          tripId={tripId}
          statusCodeDescription={statusCodeDescription}
          approver={approver}
          onClose={hide}
        />
      )}
    </>
  );
};

export default Statuses;

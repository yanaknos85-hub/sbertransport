import React, { FC } from 'react';
import { Button } from 'antd';
import { InfoCircleOutlined } from '@ant-design/icons';

import type { UUID } from 'utils/io-ts';
import { useModalState } from 'shared/hooks/useModal';
import { TransportTypes } from 'stores/TransportTypes/TransportTypes.interface';
import StatusesHistory from 'components/StatusesHistory/StatusesHistory';
import styles from './styles.module.scss';

interface OrderStatus {
  name: string;
  rusName: string;
  finalStatus?: boolean;
  backgroundColor?: string;
}

interface OrderStatusProps {
  status: OrderStatus | undefined;
  transportType: TransportTypes;
  isSharedRideSub?: boolean;
  tripId: UUID;
  statusCodeDescription: string | undefined;
  approver: {
    id: UUID;
    fullName: string;
  } | null;
}

const OrderStatus: FC<OrderStatusProps> = ({
  status,
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
        type="text"
        className={styles.status}
        style={{ backgroundColor: `${status?.backgroundColor}` }}
        onClick={show}
      >
        <span>{status?.rusName}</span>
        <InfoCircleOutlined style={{ color: `${status?.backgroundColor}` }} />
      </Button>

      {visible && (
        <StatusesHistory
          visible={visible}
          transportType={transportType}
          isSharedRideSub={isSharedRideSub}
          statusCodeDescription={statusCodeDescription}
          tripId={tripId}
          onClose={hide}
          approver={approver}
        />
      )}
    </>
  );
};

export default OrderStatus;

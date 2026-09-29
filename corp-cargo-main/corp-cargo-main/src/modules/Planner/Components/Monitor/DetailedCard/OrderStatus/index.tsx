import React, { FC } from 'react';

import styles from './styles.module.scss';

interface OrderStatus {
  name: string;
  rusName: string;
  finalStatus?: boolean;
  color?: string;
}

interface OrderStatusProps {
  status: OrderStatus | undefined;
}

const OrderStatus: FC<OrderStatusProps> = ({ status }) => (
  <span className={styles.ordersStatus} style={{ backgroundColor: `${status?.color}` }}>
    {status?.rusName}
  </span>
);

export default OrderStatus;

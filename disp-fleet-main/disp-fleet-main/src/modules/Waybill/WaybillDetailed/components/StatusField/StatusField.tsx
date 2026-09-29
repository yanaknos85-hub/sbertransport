import React from 'react';
import type { FC } from 'react';
import cn from 'classnames';

import { DeclinedWaybillStatuses, WaybillStatus, WaybillStatusesNames } from 'api/waybill/waybill.constants';

import styles from './styles.module.scss';

interface Props {
  status: WaybillStatus;
}

const StatusField: FC<Props> = ({ status }) => {
  return (
    <div className={cn(styles.container, {
      [styles.container_finished]: status === WaybillStatus.EWB_CLOSED,
      [styles.container_canceled]: DeclinedWaybillStatuses.includes(status),
    })}
    >
      <span>{WaybillStatusesNames[status]?.toUpperCase()}</span>
    </div>
  );
};

export default StatusField;

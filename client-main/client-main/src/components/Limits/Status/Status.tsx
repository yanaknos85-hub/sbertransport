import React, { FC } from 'react';
import cn from 'classnames';

import { LIMIT_REQUEST_STATUS } from 'stores/Limits/Limit.interface';
import styles from './styles.module.scss';

interface LimitStatusProps {
  status: string;
  children: string;
}

const Status: FC<LimitStatusProps> = ({ status, children }) => (
  <div
    className={cn(styles.limitStatus, {
      [styles.limitStatusWait]: status === LIMIT_REQUEST_STATUS.INIT,
      [styles.limitStatusCompleted]:
        status === LIMIT_REQUEST_STATUS.DONE_FULLY || status === LIMIT_REQUEST_STATUS.DONE_PARTLY,
      [styles.limitStatusCanceled]:
        status === LIMIT_REQUEST_STATUS.DECLINED || status === LIMIT_REQUEST_STATUS.CANCELLED,
    })}
  >
    {children}
  </div>
);

export default Status;

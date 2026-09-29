import React, { FC } from 'react';
import cn from 'classnames';
import moment from 'moment';
import StatusTag from 'shared/components/Cargo/StatusTag/StatusTag';
import { useUiContext } from 'shared/components/UI';

import { DATE_FORMAT } from 'constants/constants.app';

import {
  CargoRequestStatusesTitlesExchange,
  CargoRequestStatusesTypesExchange
} from '../../../ExchangeStatuses.constants';
import { ListItemExchange } from '../../../types';

import styles from './Header.module.scss';

interface Props {
  request: ListItemExchange;
}

export const Header: FC<Props> = props => {
  const { request } = props;

  const { isMobile } = useUiContext();

  return (
    <div className={(cn(
      styles.header, {
        [styles['isMobileDirection']]: isMobile,
        [styles['headerMobile']]: isMobile,
      }))}
    >
      <div className={cn(styles.data, {
        [styles['isMobileDirection']]: isMobile,
        [styles['dataMobile']]: isMobile,
      })}
      >
        <div className={styles.humanId}>{request.humanReadableId}</div>
        <div className={styles.desiredDate}>
          <span className={styles.dateTitle}>Забрать в</span>
          {' '}
          <span className={styles.date}>
            {moment(request.desiredDate).format(DATE_FORMAT.DATE_WITH_TIME_DOTS_REVERTED)}
          </span>
        </div>
      </div>
      <div>
        <StatusTag
          type={request?.status && CargoRequestStatusesTypesExchange[request?.status]}
        >
          {request?.status && CargoRequestStatusesTitlesExchange[request?.status]}
        </StatusTag>
      </div>
    </div>
  );
};

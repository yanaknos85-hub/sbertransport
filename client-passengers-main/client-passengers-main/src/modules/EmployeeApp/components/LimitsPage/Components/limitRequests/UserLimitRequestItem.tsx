/* eslint-disable @typescript-eslint/no-explicit-any */
import { Descriptions, List } from 'antd';
import moment from 'moment';
import React from 'react';

import { DATE_FORMAT } from 'constants/constants.app';

import { LimitRequestStatusesTitlesEnum } from 'stores/Limits/Limit.interface';
import { TransportTypeTitlesEnum } from 'stores/Trip/Trip.interface';
import { formatRubles } from 'utils';

import styles from '../../../ApprovalPage/LimitRequestPage/LimitRequestList/item.module.scss';
import { LimitRequestPeriods } from '../../LimitRequestModal/constants';
import { ISpentActionsType } from '../../useDetailedLimitsMapper';
import { isRequestCancelled } from '../../utils';

export const UserLimitRequestItem: React.FC<{
  request: ISpentActionsType;
  itemClickHandler: any;
}> = ({ request, itemClickHandler }) => (
  <List.Item
    className={styles.listItem}
    onClick={(): void => {
      itemClickHandler(request.id);
    }}
  >
    <div className={`${styles.info} ${styles.infoBottom}`}>
      <Descriptions size="middle" column={3}>
        <Descriptions.Item span={3}>
          <span className={styles.date}>
            {request.creationTime ? moment(request.creationTime).format(DATE_FORMAT.DATE_WITH_TIME) : '-'}
          </span>
        </Descriptions.Item>
        <Descriptions.Item label="Ресурс" span={3}>
          <span>
            {request.transportType
              ? TransportTypeTitlesEnum[request.transportType as keyof typeof TransportTypeTitlesEnum]
              : '-'}
          </span>
        </Descriptions.Item>
        <Descriptions.Item label="Сумма" span={3}>
          <span>{request.sum ? formatRubles(request.sum) : 0}</span>
        </Descriptions.Item>
        <Descriptions.Item label="Период" span={3}>
          <span>{LimitRequestPeriods[request.period]}</span>
        </Descriptions.Item>
        <Descriptions.Item label="Статус" span={3}>
          <span>
            {request.status
              ? LimitRequestStatusesTitlesEnum[request.status as keyof typeof LimitRequestStatusesTitlesEnum]
              : '-'}
          </span>
        </Descriptions.Item>
        {isRequestCancelled(request.status) && (
          <Descriptions.Item label="Причина" span={3}>
            <span>{request.declineReason || '-'}</span>
          </Descriptions.Item>
        )}
      </Descriptions>
    </div>
  </List.Item>
);

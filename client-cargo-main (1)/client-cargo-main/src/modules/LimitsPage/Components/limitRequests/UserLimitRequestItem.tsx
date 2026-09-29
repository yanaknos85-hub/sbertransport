import React from 'react';
import { Descriptions, List } from 'antd';
import moment from 'moment';
import { emptySign } from 'shared/constants/constants';
import { formatRubles } from 'utils';

import { LimitRequestStatusesTitlesEnum } from 'stores/Limits/Limit.interface';
import { TransportTypeTitlesEnum } from 'stores/Trip/Trip.interface';
import { DATE_FORMAT } from 'constants/constants.app';

import { LimitRequestPeriods } from '../../LimitRequestModal/constants';
import { ISpentActionsType } from '../../useDetailedLimitsMapper';
import { isRequestCancelled } from '../../utils';

import styles from '../../../ApprovalPage/LimitRequestPage/LimitRequestList/item.module.scss';

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
            {request.creationTime ? moment(request.creationTime).format(DATE_FORMAT.DATE_WITH_TIME) : emptySign}
          </span>
        </Descriptions.Item>
        <Descriptions.Item label="Ресурс" span={3}>
          <span>
            {request.transportType
              ? TransportTypeTitlesEnum[request.transportType as keyof typeof TransportTypeTitlesEnum]
              : emptySign}
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
              : emptySign}
          </span>
        </Descriptions.Item>
        {isRequestCancelled(request.status) && (
          <Descriptions.Item label="Причина" span={3}>
            <span>{request.declineReason || emptySign}</span>
          </Descriptions.Item>
        )}
      </Descriptions>
    </div>
  </List.Item>
);

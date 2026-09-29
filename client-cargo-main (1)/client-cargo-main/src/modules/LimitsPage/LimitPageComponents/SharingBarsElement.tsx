import React from 'react';
import { List } from 'antd';
import { LimitBar } from 'shared/components/LimitBar/LimitBar';
import { toRubles } from 'utils';

import { LIMIT_TYPE, LimitSharing } from 'stores/Limits/Limit.interface';
import { TransportTypeTitlesEnum } from 'stores/Trip/Trip.interface';

import { EmptyFactory } from '../../../shared/EmptyFactory';
import { getAvailablePercentage } from '../../CreateTripRequest/utils/utils';

import styles from '../page.module.scss';

export const SharingBarsElement: React.FC<{
  title: string;
  errorMsg: string;
  sharing: LimitSharing[];
  type: LIMIT_TYPE;
  isDepartmentHead: boolean;
}> = ({
  title, errorMsg, sharing, type, isDepartmentHead,
}) => (
  <List
    className={styles.list}
    header={<div className="list-header">{title}</div>}
    dataSource={sharing.sort((a, b) => a.transportType.localeCompare(b.transportType)) ?? []}
    locale={{ emptyText: <EmptyFactory message={errorMsg} /> }}
    renderItem={(limitSharing): JSX.Element => (
      <List.Item>
        <LimitBar
          key={limitSharing.transportType}
          className={styles.wrapper}
          isDepartmentHead={isDepartmentHead}
          showRubles={isDepartmentHead || type === LIMIT_TYPE.EMPLOYEE}
          percent={getAvailablePercentage(limitSharing)}
          value={toRubles(limitSharing?.limitSharingPerPeriodDTO?.balance ?? 0)}
          transportType={limitSharing.transportType}
          name={(
            <span>
              {TransportTypeTitlesEnum[`${limitSharing.transportType}` as keyof typeof TransportTypeTitlesEnum]}
            </span>
          )}
          limitType={type}
          showInfo={true}
        />
      </List.Item>
    )}
  />
);

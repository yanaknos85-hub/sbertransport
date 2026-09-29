import React, { FC, useMemo } from 'react';
import cn from 'classnames';

import styles from './Statistics.module.scss';
import { useProfile } from 'api/profile/profile.api';
import { useStatisticQuery } from './useStatisticQuery';
import { activeTripStatuses, distribTripStatuses, notDistribTripStatuses } from 'constants/trips.constants';
import { useCargoTripStatistic } from 'api/trips-cargo/trips-cargo.api';
import { TripStatisticStatuses, TripStatisticStatusesTitles } from 'modules/Trips/constants';

export const Statistics: FC = () => {
  const { contractorId } = useProfile().data;
  const statistic = useCargoTripStatistic(contractorId, { suspense: false }).data;

  const [activeStatisticType, setActiveStatistic] = useStatisticQuery();

  const tripStatistic = useMemo(
    () => [
      {
        type: TripStatisticStatuses.TotalCount,
        count: statistic?.totalCount,
        color: '#000000',
        statuses: activeTripStatuses,
      },
      {
        type: TripStatisticStatuses.AssignCount,
        count: statistic?.assignCount,
        color: '#10BF6A',
        statuses: distribTripStatuses,
      },
      {
        type: TripStatisticStatuses.NotAssignCount,
        count: statistic?.notAssignCount,
        color: '#FF5743',
        statuses: notDistribTripStatuses,
      },
    ],
    [statistic]
  );

  return (
    <div className={styles.container}>
      {tripStatistic.map(({
        type, count, color, statuses,
      }) => (
        <div
          key={type}
          className={cn(styles.status, {
            [styles.activeStatus]: activeStatisticType === type,
          })}
          onClick={() => setActiveStatistic(type, statuses)}
        >
          {TripStatisticStatusesTitles[type]}
          <span className={styles.count} style={{ color }}>
            {count || '-'}
          </span>
        </div>
      ))}
    </div>
  );
};

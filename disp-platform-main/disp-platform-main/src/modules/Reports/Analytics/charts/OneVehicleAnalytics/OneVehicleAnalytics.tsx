import React, { FC } from 'react';
import { useTranslation } from 'i18n';
import moment from 'moment';
import cn from 'classnames';

import { useOneVehicleExploitation } from 'api/analytics/analytics.api';
import { useAnalyticsQuery } from '../../context/AnalyticsQuery';
import ChartCard from '../../components/ChartCard/ChartCard';

import styles from './OneVehicleAnalytics.module.scss';

export const calculateExploitationTime = (exploitationStart: string) => {
  let diff = moment().diff(exploitationStart, 'months');
  let unit: 'm' | 'd' = 'm';

  if (!diff) {
    diff = moment().diff(exploitationStart, 'days');
    unit = 'd';
  }

  return {
    value: diff,
    text: unit === 'm' ? 'мес.' : 'дней',
  };
};

const OneVehicleAnalytics: FC = () => {
  const t = useTranslation().t.Analytics.OneVehicleAnalytics;

  const { stateNumbers, ...filters } = useAnalyticsQuery().query;

  const stateNumber = stateNumbers?.[0] ?? '';

  const { data } = useOneVehicleExploitation({
    ...filters,
    stateNumber,
  });

  const {
    value: inExploitationDays,
    text,
  } = calculateExploitationTime(data.exploitationStart);

  return (
    <ChartCard className={styles.container} title={t.title}>
      <div className={styles.data}>
        <div className={cn(styles.analytics, styles.exploitationTime)}>
          <div className={styles.title}>{`${t.exploitationTime}, ${text}`}</div>
          <div className={styles.value}>{inExploitationDays}</div>
        </div>
        <div className={styles.analytics}>
          <div className={styles.title}>{t.exploitationStart}</div>
          <div className={styles.value}>{moment(data.exploitationStart).format('DD.MM.YYYY')}</div>
        </div>
      </div>
    </ChartCard>
  );
};

export default OneVehicleAnalytics;

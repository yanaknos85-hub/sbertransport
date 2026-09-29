import React, {
  FC, Suspense, useLayoutEffect
} from 'react';
import { useHistory } from 'react-router-dom';
import { useTranslation } from 'i18n';

import { useSelfAutopark } from 'api/contractors/contractors.api';
import ErrorBoundary from 'components/ErrorBoundary';
import SpinWrapped from 'components/SpinWrapped/SpinWrapped';

import { REPORTS_PASSENGER_TRIPS_ROUTE } from '../ReportsRouter';
import Filters from './components/Filters/Filters';
import VehicleAnalytics from './charts/VehicleAnalytics/VehicleAnalytics';
import RepairAnalytics from './charts/RepairAnalytics/RepairAnalytics';
import MileageCostAnalytics from './charts/MileageCostAnalytics/MileageCostAnalytics';
import FuelConsumptionAnalytics from './charts/FuelConsumptionAnalytics/FuelConsumptionAnalytics';
import UpdateAnalytics from './charts/UpdateAnalytics/UpdateAnalytics';
import OneVehicleAnalytics from './charts/OneVehicleAnalytics/OneVehicleAnalytics';
import { AnalyticsQueryProvider, useAnalyticsQuery } from './context/AnalyticsQuery';

import styles from './Analytics.module.scss';

export const Charts: FC = () => {
  const { query } = useAnalyticsQuery();

  return (
    <div className={styles.container}>
      <div className={styles.wrapper}>
        <ErrorBoundary>
          <Suspense fallback={<SpinWrapped />}>
            {query.stateNumbers?.length === 1 ? <OneVehicleAnalytics /> : <VehicleAnalytics />}
          </Suspense>
        </ErrorBoundary>
        <ErrorBoundary>
          <Suspense fallback={<SpinWrapped />}>
            <RepairAnalytics />
          </Suspense>
        </ErrorBoundary>
      </div>
      <div className={styles.wrapper}>
        <ErrorBoundary>
          <Suspense fallback={<SpinWrapped />}>
            <MileageCostAnalytics />
          </Suspense>
        </ErrorBoundary>
        <ErrorBoundary>
          <Suspense fallback={<SpinWrapped />}>
            <FuelConsumptionAnalytics />
          </Suspense>
        </ErrorBoundary>
      </div>
      <ErrorBoundary>
        <Suspense fallback={<SpinWrapped />}>
          <UpdateAnalytics />
        </Suspense>
      </ErrorBoundary>
    </div>
  );
};

const Analytics: FC = () => {
  const history = useHistory();
  const { t } = useTranslation();

  const { isInternal: accessAnalytics } = useSelfAutopark().data;

  useLayoutEffect(() => {
    if (!accessAnalytics) {
      history.push({
        ...history.location,
        pathname: REPORTS_PASSENGER_TRIPS_ROUTE,
      });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [accessAnalytics]);

  return (
    <div className={styles.analytics}>
      <div className={styles.header}>
        <h2 className={styles.title}>{t.Analytics.title}</h2>
        <span className={styles.warning}>Раздел находится в разработке</span>
      </div>

      <AnalyticsQueryProvider>
        <Filters />

        <Charts />
      </AnalyticsQueryProvider>
    </div>
  );
};

export default Analytics;

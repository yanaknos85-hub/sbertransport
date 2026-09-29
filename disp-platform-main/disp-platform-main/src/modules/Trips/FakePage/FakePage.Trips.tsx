import React, { FC, Suspense, lazy } from 'react';
import { useHistory, useParams, useRouteMatch } from 'react-router-dom';
import { Tabs } from 'antd';
import ErrorBoundary from 'components/ErrorBoundary';
import SpinWrapped from 'components/SpinWrapped/SpinWrapped';
import { TripType } from 'constants/app.constants';

import styles from './FakePage.module.scss';

const PassengerTrips = lazy(() => import('../Trips'));

const FakePage: FC = () => {
  const { type } = useParams<{ type: TripType }>();
  const { path } = useRouteMatch();
  const { replace } = useHistory();

  const handleChangeTab = (value: string) => {
    replace(path.replace(':type', value));
  };

  return (
    <div className={styles.container}>
      <Tabs
        className={styles.tripTypeTabs}
        activeKey={type}
        onChange={handleChangeTab}
        size="small"
        destroyInactiveTabPane
      >
        <Tabs.TabPane tab="Пассажирские перевозки" key={TripType.Passenger}>
          <ErrorBoundary>
            <Suspense fallback={<SpinWrapped />}>
              <PassengerTrips />
            </Suspense>
          </ErrorBoundary>
        </Tabs.TabPane>

        <Tabs.TabPane tab="Грузовые перевозки" key={TripType.Cargo}>
          <ErrorBoundary>
            <Suspense fallback={<SpinWrapped />}>
              <div>Демо</div>
            </Suspense>
          </ErrorBoundary>
        </Tabs.TabPane>
      </Tabs>
    </div>
  );
};

export default FakePage;

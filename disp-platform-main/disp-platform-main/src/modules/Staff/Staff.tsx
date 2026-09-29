import React, { FC, lazy, Suspense } from 'react';
import { useHistory, useParams, useRouteMatch } from 'react-router-dom';
import { Tabs } from 'antd';

import { AppTitles, StaffRoles } from 'constants/app.constants';
import ErrorBoundary from 'components/ErrorBoundary';
import SpinWrapped from 'components/SpinWrapped/SpinWrapped';
import styles from './styles.module.scss';

const Drivers = lazy(() => import('./Drivers'));
const Dispatchers = lazy(() => import('./Dispatchers'));

const Staff: FC = () => {
  const { role } = useParams<{ role: StaffRoles }>();
  const { path } = useRouteMatch();
  const { replace } = useHistory();

  const handleChangeTab = (value: string) => {
    replace(path.replace(':role', value));
  };

  return (
    <div className={styles.container}>
      <Tabs
        className={styles.staffTabs}
        activeKey={role}
        onChange={handleChangeTab}
        size="small"
        destroyInactiveTabPane
      >
        <Tabs.TabPane tab={AppTitles.Drivers} key={StaffRoles.Drivers}>
          <ErrorBoundary>
            <Suspense fallback={<SpinWrapped />}>
              <Drivers />
            </Suspense>
          </ErrorBoundary>
        </Tabs.TabPane>

        <Tabs.TabPane tab={AppTitles.Dispatchers} key={StaffRoles.Dispatchers}>
          <ErrorBoundary>
            <Suspense fallback={<SpinWrapped />}>
              <Dispatchers />
            </Suspense>
          </ErrorBoundary>
        </Tabs.TabPane>
      </Tabs>
    </div>
  );
};

export default Staff;

import React, { FC, Suspense } from 'react';
import { useParams, useRouteMatch } from 'react-router-dom';
import { useHistory } from '@sber-sbertransport/mf-core';
import mfLoader from 'mf/MFLoader';
import { AUTOPARKS_TAB } from 'constants/constants.routes';
import { TabPane, Tabs } from 'shared/components/Tabs';
import SpinWrapped from 'shared/components/SpinWrapped/SpinWrapped';
import ErrorBoundary from 'shared/components/ErrorBoundary';
import { AutoparkTabs } from './Autopark.constants';

import styles from './styles.module.scss';

const AutoparksList = React.lazy(() => mfLoader(import('platform/modules/Autopark')));
const Directories = React.lazy(() => mfLoader(import('fleet/modules/FleetManagement/Directories')));

const Autopark: FC = () => {
  const { autoparkTab } = useParams();
  const history = useHistory();
  const { isExact } = useRouteMatch();

  const onTabChange = (tab: string) => {
    history.replace({
      pathname: AUTOPARKS_TAB.replace(':autoparkTab', tab),
    });
  };

  return (
    <Tabs
      activeKey={autoparkTab}
      onChange={onTabChange}
      destroyInactiveTabPane
      className={!isExact ? styles.hiddenTabs : undefined}
    >
      <TabPane tab="Автопарки" key={AutoparkTabs.List}>
        <ErrorBoundary>
          <Suspense fallback={<SpinWrapped />}>
            <AutoparksList baseUrl={AUTOPARKS_TAB.replace(':autoparkTab', AutoparkTabs.List)} />
          </Suspense>
        </ErrorBoundary>
      </TabPane>
      <TabPane tab="Справочники ТС" key={AutoparkTabs.Directories}>
        <ErrorBoundary>
          <Suspense fallback={<SpinWrapped />}>
            <Directories newDesign />
          </Suspense>
        </ErrorBoundary>
      </TabPane>
    </Tabs>
  );
};

export default Autopark;

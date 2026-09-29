import React, { Suspense } from 'react';
import { useHistory, useRouteMatch, useParams } from 'react-router-dom';

import ErrorBoundary from 'shared/components/ErrorBoundary/ErrorBoundary';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { TabPane, Tabs as PaneTabs } from 'shared/components/Tabs';

import {
  serviceTypes, serviceTypesTitles, TProps, ServicesTabs
} from '../../constants/tripSettings';

import TransportTypesContent from './TransportTypesContent';

import styles from './TransportTypes.module.scss';

export const TransportTypes: React.FC<TProps> = () => {
  const { type } = useParams<{ type: ServicesTabs }>();
  const { path } = useRouteMatch();
  const { replace } = useHistory();

  const handleChangeTab = (value: string) => {
    replace(path.replace(':type', value));
  };

  return (
    <div className={styles.TransportTypesWrapper}>
      <Suspense fallback={<SpinWrapped />}>
        <PaneTabs activeKey={type} onChange={handleChangeTab}>
          <TabPane
            key={ServicesTabs.Passengers}
            tab={serviceTypesTitles[serviceTypes.EMPLOYEE_TRANSPORTATION]}
            className={styles.switchesColumn}
          >
            <ErrorBoundary>
              <React.Suspense fallback={<SpinWrapped />}>
                <TransportTypesContent />
              </React.Suspense>
            </ErrorBoundary>
          </TabPane>
        </PaneTabs>
      </Suspense>
    </div>
  );
};

export default TransportTypes;

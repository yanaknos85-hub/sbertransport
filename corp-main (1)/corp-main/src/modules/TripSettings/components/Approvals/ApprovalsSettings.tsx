import React from 'react';
import { Tabs } from 'antd';
import ErrorBoundary from 'shared/components/ErrorBoundary/ErrorBoundary';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { StepItemContentProps } from 'shared/components/Steps';
import mfLoader from 'mf/MFLoader';
import { serviceTypes, serviceTypesTitles } from '../../constants/tripSettings';
import { useTripSettingsContext } from '../../context/TripSettings.context';

const TripSettingsPassengersProvider = React.lazy(() => mfLoader(import('passengers/modules/TripSettings/TripSettingsProvider')));
const TripSettingsPassengersApprovals = React.lazy(() => mfLoader(import('passengers/modules/TripSettings/Approvals')));

const TripSettingsCargoProvider = React.lazy(() => mfLoader(import('cargo/modules/TripSettings/TripSettingsProvider')));
const TripSettingsCargoApprovals = React.lazy(() => mfLoader(import('cargo/modules/TripSettings/Approvals')));

import styles from '../../TripSettings.module.scss';

export const ApprovalsSettings: React.FC<StepItemContentProps> = () => {
  const tripSettingsContext = useTripSettingsContext();

  return (
    <div className={styles.ApprovalsSettingsWrapper}>
      <Tabs>
        <Tabs.TabPane
          tab={serviceTypesTitles[serviceTypes.EMPLOYEE_TRANSPORTATION]}
          key={serviceTypes.EMPLOYEE_TRANSPORTATION}
        >
          <ErrorBoundary>
            <React.Suspense fallback={<SpinWrapped />}>
              <TripSettingsPassengersProvider value={tripSettingsContext}>
                <TripSettingsPassengersApprovals />
              </TripSettingsPassengersProvider>
            </React.Suspense>
          </ErrorBoundary>
        </Tabs.TabPane>
        <Tabs.TabPane
          tab={serviceTypesTitles[serviceTypes.CARGO_TRANSPORTATION]}
          key={serviceTypes.CARGO_TRANSPORTATION}
          disabled
        >
          <ErrorBoundary>
            <React.Suspense fallback={<SpinWrapped />}>
              <TripSettingsCargoProvider value={tripSettingsContext}>
                <TripSettingsCargoApprovals />
              </TripSettingsCargoProvider>
            </React.Suspense>
          </ErrorBoundary>
        </Tabs.TabPane>
      </Tabs>
    </div>
  );
};

export default ApprovalsSettings;

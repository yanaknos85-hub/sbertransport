import React from 'react';
import { Tabs } from 'antd';
import ErrorBoundary from 'shared/components/ErrorBoundary/ErrorBoundary';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { StepItemContentProps } from 'shared/components/Steps';
import mfLoader from 'mf/MFLoader';
import { serviceTypes, serviceTypesTitles } from '../../constants/tripSettings';
import { useTripSettingsContext } from '../../context/TripSettings.context';

const TripSettingsPassengersProvider = React.lazy(() => mfLoader(import('passengers/modules/TripSettings/TripSettingsProvider')));
const TripSettingsPassengersSharedRide = React.lazy(() => mfLoader(import('passengers/modules/TripSettings/SharedRideSettings')));

const TripSettingsCargoProvider = React.lazy(() => mfLoader(import('cargo/modules/TripSettings/TripSettingsProvider')));
const TripSettingsCargoSharedRide = React.lazy(() => mfLoader(import('cargo/modules/TripSettings/SharedRideSettings')));

import styles from '../../TripSettings.module.scss';

export const SharedRideSettings: React.FC<StepItemContentProps> = () => {
  const tripSettingsContext = useTripSettingsContext();

  return (
    <div className={styles.sharedRidesSettingsWrapper}>
      <Tabs>
        <Tabs.TabPane
          tab={serviceTypesTitles[serviceTypes.EMPLOYEE_TRANSPORTATION]}
          key={serviceTypes.EMPLOYEE_TRANSPORTATION}
        >
          <ErrorBoundary>
            <React.Suspense fallback={<SpinWrapped />}>
              <TripSettingsPassengersProvider value={tripSettingsContext}>
                <TripSettingsPassengersSharedRide />
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
                <TripSettingsCargoSharedRide />
              </TripSettingsCargoProvider>
            </React.Suspense>
          </ErrorBoundary>
        </Tabs.TabPane>
      </Tabs>
    </div>
  );
};

export default SharedRideSettings;

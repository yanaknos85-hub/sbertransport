import React, { Suspense } from 'react';
import { useHistory, useParams, useRouteMatch } from 'react-router-dom';
import ErrorBoundary from 'shared/components/ErrorBoundary/ErrorBoundary';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { TabPane, Tabs as PaneTabs } from 'shared/components/Tabs';

import mfLoader from 'mf/MFLoader';

import {
  serviceTypes, serviceTypesTitles, TProps, ServicesTabs
} from '../../constants/tripSettings';
import { useTripSettingsContext } from '../../context/TripSettings.context';

const TripSettingsPassengersProvider = React.lazy(() => mfLoader(import('passengers/modules/TripSettings/TripSettingsProvider')));
const TripSettingsPassengersTransportTypes = React.lazy(() => mfLoader(import('passengers/modules/TripSettings/TransportTypes')));

const TripSettingsCargoProvider = React.lazy(() => mfLoader(import('cargo/modules/TripSettings/TripSettingsProvider')));
const TripSettingsCargoTransportTypes = React.lazy(() => mfLoader(import('cargo/modules/TripSettings/TransportTypes')));

const TripSettingsFleetProvider = React.lazy(() => mfLoader(import('fleet/modules/TripSettings/TripSettingsProvider')));
const TripSettingsFleetTransportTypes = React.lazy(() => mfLoader(import('fleet/modules/TripSettings/TransportTypes')));

import styles from './TransportTypes.module.scss';

export const TransportTypes: React.FC<TProps> = () => {
  const tripSettingsContext = useTripSettingsContext();

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
                <TripSettingsPassengersProvider value={tripSettingsContext}>
                  <TripSettingsPassengersTransportTypes />
                </TripSettingsPassengersProvider>
              </React.Suspense>
            </ErrorBoundary>
          </TabPane>
          <TabPane
            key={ServicesTabs.Cargo}
            tab={serviceTypesTitles[serviceTypes.CARGO_TRANSPORTATION]}
            className={styles.switchesColumn}
          >
            <ErrorBoundary>
              <React.Suspense fallback={<SpinWrapped />}>
                <TripSettingsCargoProvider value={tripSettingsContext}>
                  <TripSettingsCargoTransportTypes />
                </TripSettingsCargoProvider>
              </React.Suspense>
            </ErrorBoundary>
          </TabPane>
          <TabPane
            key={ServicesTabs.Fleet}
            tab={serviceTypesTitles[serviceTypes.FLEET_MANAGEMENT]}
            className={styles.switchesColumn}
          >
            <ErrorBoundary>
              <React.Suspense fallback={<SpinWrapped />}>
                <TripSettingsFleetProvider value={tripSettingsContext}>
                  <TripSettingsFleetTransportTypes />
                </TripSettingsFleetProvider>
              </React.Suspense>
            </ErrorBoundary>
          </TabPane>
        </PaneTabs>
      </Suspense>
    </div>
  );
};

export default TransportTypes;

import React from 'react';
import type { FC } from 'react';
import { useHistory, useParams, useRouteMatch } from 'react-router-dom';
import ErrorBoundary from 'shared/components/ErrorBoundary/ErrorBoundary';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';

import mfLoader from 'mf/MFLoader';
import { useTranslation } from 'i18n';
import { TabPane, Tabs } from 'shared/components/Tabs';
import { ContractsTabs } from '../../constants';
import { useTariffSettingsContext } from '../../context/TariffSettings.context';

const TariffSettingsPassengersProvider = React.lazy(() => mfLoader(import('passengers/modules/TariffSettings/TariffSettingsProvider')));
const TariffSettingsPassengers = React.lazy(() => mfLoader(import('passengers/modules/TariffSettings/Contracts')));

const TariffSettingsCargoProvider = React.lazy(() => mfLoader(import('cargo/modules/TariffSettings/TariffSettingsProvider')));
const TariffSettingsCargo = React.lazy(() => mfLoader(import('cargo/modules/TariffSettings/Contracts')));

const TariffSettingsFleetProvider = React.lazy(() => mfLoader(import('fleet/modules/TariffSettings/TariffSettingsProvider')));
const TariffSettingsCarService = React.lazy(() => mfLoader(import('fleet/modules/TariffSettings/Contracts/CarService')));
const TariffSettingsEwb = React.lazy(() => mfLoader(import('fleet/modules/TariffSettings/Contracts/Ewb')));
const TariffSettingsGasStations = React.lazy(() => mfLoader(import('fleet/modules/TariffSettings/Contracts/GasStations')));

const Contracts: FC = () => {
  const { t } = useTranslation();
  const { type, routeId } = useParams<{ type: ContractsTabs; routeId: string }>();
  const { path } = useRouteMatch();
  const { replace } = useHistory();
  const tariffSettingsContext = useTariffSettingsContext();

  const handleChangeTab = (value: string) => {
    replace(path.replace(':type', value).replace(':routeId', routeId));
  };

  return (
    <Tabs
      activeKey={type}
      onChange={handleChangeTab}
      destroyInactiveTabPane
    >
      <TabPane key={ContractsTabs.Passengers} tab={t.ServiceType.passengers}>
        <ErrorBoundary>
          <React.Suspense fallback={<SpinWrapped />}>
            <TariffSettingsPassengersProvider value={tariffSettingsContext}>
              <TariffSettingsPassengers />
            </TariffSettingsPassengersProvider>
          </React.Suspense>
        </ErrorBoundary>
      </TabPane>
      <TabPane key={ContractsTabs.Cargo} tab={t.ServiceType.cargo}>
        <ErrorBoundary>
          <React.Suspense fallback={<SpinWrapped />}>
            <TariffSettingsCargoProvider value={tariffSettingsContext}>
              <TariffSettingsCargo />
            </TariffSettingsCargoProvider>
          </React.Suspense>
        </ErrorBoundary>
      </TabPane>
      <TabPane key={ContractsTabs.CarService} tab={t.ServiceType.carService}>
        <ErrorBoundary>
          <React.Suspense fallback={<SpinWrapped />}>
            <TariffSettingsFleetProvider value={tariffSettingsContext}>
              <TariffSettingsCarService />
            </TariffSettingsFleetProvider>
          </React.Suspense>
        </ErrorBoundary>
      </TabPane>
      <TabPane key={ContractsTabs.Ewb} tab={t.ServiceType.ewb}>
        <ErrorBoundary>
          <React.Suspense fallback={<SpinWrapped />}>
            <TariffSettingsFleetProvider value={tariffSettingsContext}>
              <TariffSettingsEwb />
            </TariffSettingsFleetProvider>
          </React.Suspense>
        </ErrorBoundary>
      </TabPane>
      <TabPane key={ContractsTabs.GasStations} tab={t.ServiceType.gasStations}>
        <ErrorBoundary>
          <React.Suspense fallback={<SpinWrapped />}>
            <TariffSettingsFleetProvider value={tariffSettingsContext}>
              <TariffSettingsGasStations />
            </TariffSettingsFleetProvider>
          </React.Suspense>
        </ErrorBoundary>
      </TabPane>
    </Tabs>
  );
};

export default Contracts;

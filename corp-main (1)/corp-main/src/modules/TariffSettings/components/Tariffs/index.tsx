import React from 'react';
import { useHistory, useParams, useRouteMatch } from 'react-router-dom';
import { useTranslation } from 'i18n';
import ErrorBoundary from 'shared/components/ErrorBoundary/ErrorBoundary';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { checkPermissions } from 'shared/components/AccessControl';
import { useRole } from 'utils/useRole';
import mfLoader from 'mf/MFLoader';
import { TabPane, Tabs } from 'shared/components/Tabs';
import { TariffsTabs } from '../../constants';
import { useTariffSettingsContext } from '../../context/TariffSettings.context';
import { Roles } from 'constants/constants.app';

const TariffSettingsPassengersProvider = React.lazy(() => mfLoader(import('passengers/modules/TariffSettings/TariffSettingsProvider')));
const TariffSettingsPassengers = React.lazy(() => mfLoader(import('passengers/modules/TariffSettings/Tariffs')));

const TariffSettingsCargoProvider = React.lazy(() => mfLoader(import('cargo/modules/TariffSettings/TariffSettingsProvider')));
const TariffSettingsCargo = React.lazy(() => mfLoader(import('cargo/modules/TariffSettings/Tariffs')));

const TariffSettingsFleetProvider = React.lazy(() => mfLoader(import('fleet/modules/TariffSettings/TariffSettingsProvider')));
const TariffSettingsCarService = React.lazy(() => mfLoader(import('fleet/modules/TariffSettings/Tariffs/CarService')));
const TariffSettingsEwb = React.lazy(() => mfLoader(import('fleet/modules/TariffSettings/Tariffs/Ewb')));
const TariffSettingsGasStations = React.lazy(() => mfLoader(import('fleet/modules/TariffSettings/Tariffs/GasStations')));

const Tariffs = () => {
  const { t } = useTranslation();
  const userRoles = useRole();
  const { type, routeId } = useParams<{ type: TariffsTabs; routeId: string; transportType: string }>();
  const { path } = useRouteMatch();
  const { replace } = useHistory();
  const tariffSettingsContext = useTariffSettingsContext();

  const handleChangeTab = (value: string) => {
    replace(path.replace(':type', value).replace(':routeId', routeId).replace('/:transportType', ''));
  };

  const fleetPermitted = checkPermissions({
    userPermissions: userRoles,
    allowedPermissions: [Roles.ADMIN_CORP_CLIENT, Roles.DISPATCHER_SUPPORT_SERVICE],
  });

  return (
    <Tabs
      activeKey={type}
      onChange={handleChangeTab}
      destroyInactiveTabPane
    >
      <TabPane key={TariffsTabs.Passengers} tab={t.ServiceType.passengers}>
        <ErrorBoundary>
          <React.Suspense fallback={<SpinWrapped />}>
            <TariffSettingsPassengersProvider value={tariffSettingsContext}>
              <TariffSettingsPassengers />
            </TariffSettingsPassengersProvider>
          </React.Suspense>
        </ErrorBoundary>
      </TabPane>
      <TabPane key={TariffsTabs.Cargo} tab={t.ServiceType.cargo}>
        <ErrorBoundary>
          <React.Suspense fallback={<SpinWrapped />}>
            <TariffSettingsCargoProvider value={tariffSettingsContext}>
              <TariffSettingsCargo />
            </TariffSettingsCargoProvider>
          </React.Suspense>
        </ErrorBoundary>
      </TabPane>

      {fleetPermitted && (
        <TabPane key={TariffsTabs.CarService} tab={t.ServiceType.carService}>
          <ErrorBoundary>
            <React.Suspense fallback={<SpinWrapped />}>
              <TariffSettingsFleetProvider value={tariffSettingsContext}>
                <TariffSettingsCarService />
              </TariffSettingsFleetProvider>
            </React.Suspense>
          </ErrorBoundary>
        </TabPane>
      )}

      <TabPane key={TariffsTabs.Ewb} tab={t.ServiceType.ewb}>
        <ErrorBoundary>
          <React.Suspense fallback={<SpinWrapped />}>
            <TariffSettingsFleetProvider value={tariffSettingsContext}>
              <TariffSettingsEwb />
            </TariffSettingsFleetProvider>
          </React.Suspense>
        </ErrorBoundary>
      </TabPane>
      <TabPane key={TariffsTabs.GasStations} tab={t.ServiceType.gasStations}>
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

export default Tariffs;

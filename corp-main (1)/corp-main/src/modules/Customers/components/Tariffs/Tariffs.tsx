import React, { useMemo, useRef } from 'react';
import type { FC } from 'react';
import { useHistory, useParams, useRouteMatch } from 'react-router-dom';
import ErrorBoundary from 'shared/components/ErrorBoundary/ErrorBoundary';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import mfLoader from 'mf/MFLoader';
import { useTranslation } from 'i18n';
import { TabPane, Tabs } from 'shared/components/Tabs';
import { TariffTypes, tariffTypesTitles } from 'constants/constants.app';
import { TariffsTabs, tariffTypeTabs } from './constants/tariffs.constants';

import styles from './tariffs.module.scss';
import { PanelStyled } from 'modules/Customers/customers.styled';

const TariffsPassengersProvider = React.lazy(() => mfLoader(import('passengers/modules/Customers/CustomersProvider')));
const TariffsPassengers = React.lazy(() => mfLoader(import('passengers/modules/Customers/Tariffs')));

const TariffsCargoProvider = React.lazy(() => mfLoader(import('cargo/modules/Customers/CustomersProvider')));
const TariffsCargo = React.lazy(() => mfLoader(import('cargo/modules/Customers/Tariffs')));

const TariffsFleetProvider = React.lazy(() => mfLoader(import('fleet/modules/TariffSettings/TariffSettingsProvider')));
const TariffsFleet = React.lazy(() => mfLoader(import('fleet/modules/TariffSettings/Tariffs')));

const Tariffs: FC = () => {
  const { t } = useTranslation();
  const {
    tariffType, type, routeId,
  } = useParams<{
    tariffType: TariffTypes;
    type: TariffsTabs;
    routeId: string;
  }>();
  const { path } = useRouteMatch();
  const { replace } = useHistory();

  const footerRef = useRef<HTMLDivElement>(null);

  const context = useMemo(() => ({
    footer: footerRef,
  }), []);

  const handleChangeTariffType = (value: string) => {
    replace(path.replace(':tariffType', value).replace(':type', type).replace(':routeId', routeId)).replace('/:transportType', '');
  };

  const handleChangeTab = (value: string) => {
    replace(path.replace(':tariffType', tariffType).replace(':type', value).replace(':routeId', routeId)).replace('/:transportType', '');
  };

  return (
    <>
      <PanelStyled>
        <Tabs
          activeKey={tariffType}
          onChange={handleChangeTariffType}
          destroyInactiveTabPane
        >
          {tariffTypeTabs.map(tab => (
            <TabPane key={tab} tab={tariffTypesTitles[tab]} />
          ))}
        </Tabs>

        <Tabs
          activeKey={type}
          onChange={handleChangeTab}
          destroyInactiveTabPane
          type="card"
          size="small"
        >
          <TabPane
            key={TariffsTabs.Passengers}
            tab={t.ServiceType.passengers}
          >
            <ErrorBoundary>
              <React.Suspense fallback={<SpinWrapped />}>
                <TariffsPassengersProvider value={context}>
                  <TariffsPassengers />
                </TariffsPassengersProvider>
              </React.Suspense>
            </ErrorBoundary>
          </TabPane>

          <TabPane
            key={TariffsTabs.Cargo}
            tab={t.ServiceType.cargo}
          >
            <ErrorBoundary>
              <React.Suspense fallback={<SpinWrapped />}>
                <TariffsCargoProvider value={context}>
                  <TariffsCargo />
                </TariffsCargoProvider>
              </React.Suspense>
            </ErrorBoundary>
          </TabPane>

          <TabPane
            key={TariffsTabs.CarService}
            tab={t.ServiceType.carService}
            disabled
          >
            <ErrorBoundary>
              <React.Suspense fallback={<SpinWrapped />}>
                <TariffsFleetProvider value={context}>
                  <TariffsFleet />
                </TariffsFleetProvider>
              </React.Suspense>
            </ErrorBoundary>
          </TabPane>
        </Tabs>
      </PanelStyled>

      <div ref={footerRef} className={styles.footer} />
    </>
  );
};

export default Tariffs;

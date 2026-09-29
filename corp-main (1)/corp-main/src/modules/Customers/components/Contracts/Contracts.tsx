import React, { useMemo, useRef } from 'react';
import type { FC } from 'react';
import { useHistory, useParams, useRouteMatch } from 'react-router-dom';
import ErrorBoundary from 'shared/components/ErrorBoundary/ErrorBoundary';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';

import mfLoader from 'mf/MFLoader';
import { useTranslation } from 'i18n';
import { TabPane, Tabs } from 'shared/components/Tabs';

import { ContractsTabs, contractTypeTabs } from './constants/contracts.constants';
import { ContractTypes, contractTypesTitles } from 'constants/constants.app';
import { PanelStyled } from 'modules/Customers/customers.styled';

import styles from './contracts.module.scss';

const CustomersPassengersProvider = React.lazy(() => mfLoader(import('passengers/modules/Customers/CustomersProvider')));
const ContractsPassengers = React.lazy(() => mfLoader(import('passengers/modules/Customers/Contracts')));

const CustomersCargoProvider = React.lazy(() => mfLoader(import('cargo/modules/Customers/CustomersProvider')));
const ContractsCargo = React.lazy(() => mfLoader(import('cargo/modules/Customers/Contracts')));

const CustomersFleetProvider = React.lazy(() => mfLoader(import('fleet/modules/TariffSettings/TariffSettingsProvider')));
const ContractsFleet = React.lazy(() => mfLoader(import('fleet/modules/TariffSettings/Contracts')));

const Contracts: FC = () => {
  const { t } = useTranslation();
  const {
    contractType, type, routeId,
  } = useParams<{
    contractType: ContractTypes;
    type: ContractsTabs;
    routeId: string;
  }>();
  const { path } = useRouteMatch();
  const { replace } = useHistory();

  const footerRef = useRef<HTMLDivElement>(null);

  const context = useMemo(() => ({
    footer: footerRef,
  }), []);

  const handleChangeContractType = (value: string) => {
    replace(path.replace(':contractType', value).replace(':type', type).replace(':routeId', routeId));
  };

  const handleChangeTab = (value: string) => {
    replace(path.replace(':contractType', contractType).replace(':type', value).replace(':routeId', routeId));
  };

  return (
    <>
      <PanelStyled>
        <Tabs
          activeKey={contractType}
          onChange={handleChangeContractType}
          destroyInactiveTabPane
        >
          {contractTypeTabs.map(tab => (
            <TabPane key={tab} tab={contractTypesTitles[tab]} />
          ))}
        </Tabs>

        <Tabs
          activeKey={type}
          onChange={handleChangeTab}
          destroyInactiveTabPane
          size="small"
          type="card"
        >
          <TabPane
            key={ContractsTabs.Passengers}
            tab={t.ServiceType.passengers}
          >
            <ErrorBoundary>
              <React.Suspense fallback={<SpinWrapped />}>
                <CustomersPassengersProvider value={context}>
                  <ContractsPassengers />
                </CustomersPassengersProvider>
              </React.Suspense>
            </ErrorBoundary>
          </TabPane>

          <TabPane
            key={ContractsTabs.Cargo}
            tab={t.ServiceType.cargo}
          >
            <ErrorBoundary>
              <React.Suspense fallback={<SpinWrapped />}>
                <CustomersCargoProvider value={context}>
                  <ContractsCargo />
                </CustomersCargoProvider>
              </React.Suspense>
            </ErrorBoundary>
          </TabPane>

          <TabPane
            key={ContractsTabs.Ewb}
            tab={t.ServiceType.ewb}
            disabled
          >
            <ErrorBoundary>
              <React.Suspense fallback={<SpinWrapped />}>
                <CustomersFleetProvider value={context}>
                  <ContractsFleet />
                </CustomersFleetProvider>
              </React.Suspense>
            </ErrorBoundary>
          </TabPane>
        </Tabs>
      </PanelStyled>

      <div ref={footerRef} className={styles.footer} />
    </>
  );
};

export default Contracts;

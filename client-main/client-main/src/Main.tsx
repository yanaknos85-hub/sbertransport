/* eslint-disable @stylistic/object-curly-newline */
import { OrgStructureType } from '@sber-sbertransport/mf-core/dist/constants/constants';
import { AiAssistant } from '@sber-sbertransport/ai-agent';
import React, { FC } from 'react';
import { observer } from 'mobx-react';
import { Spin } from 'antd';
import mfLoader from 'mf/MFLoader';
import { queryCache } from 'constants/constants';
import { LayoutDesktop, LayoutMobile } from 'ui/Layout';
import SideMenu from 'ui/SideMenu/SideMenu';
import { SideMenuMobile } from 'ui/SideMenu/SideMenuMobile';
import { HeaderDesktop, HeaderMobile } from 'ui/Header';
import { useHook as useBreadcrumbs } from 'shared/components/Breadcrumbs';
import { useAppStore, StoreNames, hooks } from 'stores';
import AppRouter from './AppRouter';
import PassengersMemo from 'modules/PassengersMemo/PassengersMemo';
import { use2GIS } from '@sber-sbertransport/ui-kit/src';
import { usePlatformDetect } from 'shared/hooks/usePlatformDetect';
import { Route, Switch } from 'react-router-dom';
import * as routes from 'constants/constants.routes';
import MainAI from 'modules/MainAI/MainAI';

const PassengersProvider = React.lazy(() => mfLoader(import('passengers/AppProvider')));
const CargoProvider = React.lazy(() => mfLoader(import('cargo/AppProvider')));
const FleetProvider = React.lazy(() => mfLoader(import('fleet/AppProvider')));

const PersonalInfoConsent = React.lazy(() => mfLoader(import('auth/PersonalInfoConsent')));

const Main: FC = observer((): JSX.Element | null => {
  const {
    [StoreNames.selfStore]: { selfEmployee },
    http, logger,
  } = useAppStore();
  const mapContext = use2GIS();
  const breadcrumbs = useBreadcrumbs();
  const { isSelfEmployeeLoaded } = hooks.useInitStore();

  if (!isSelfEmployeeLoaded) {
    return <Spin />;
  }

  if (!selfEmployee.consent && selfEmployee.orgStructureType !== OrgStructureType.INTERNAL) {
    return <PersonalInfoConsent />;
  }

  // То что идет в провайдеры МФ от Мейна
  const mfProviderValue = {
    queryCache,
    breadcrumbs,
    mapContext,
  };

  const { isMobile, isDesktop } = usePlatformDetect();

  // Дриллинг МФ провайдеров - не лучшая идея, может есть смысл разделить по роутам!
  // но также возможно потребуется доступ в лейоут из одного из МФ. Поэтому пока так
  return (
    <PassengersProvider value={mfProviderValue}>
      <CargoProvider value={mfProviderValue}>
        <FleetProvider value={mfProviderValue}>
          <Switch>
            <Route
              path={routes.AI}
              component={MainAI}
            />

            {isDesktop && (
              <LayoutDesktop headerContent={<HeaderDesktop />} drawerContent={<SideMenu />}>
                <AppRouter />
                <AiAssistant
                  agentType="Client"
                  userId={selfEmployee?.id}
                  httpService={http}
                  onMessage={logger.toMessage}
                />
              </LayoutDesktop>
            )}

            {isMobile && (
              <LayoutMobile headerContent={<HeaderMobile drawerContent={<SideMenuMobile />} />}>
                <AppRouter />
              </LayoutMobile>
            )}
          </Switch>

          <PassengersMemo />
        </FleetProvider>
      </CargoProvider>
    </PassengersProvider>
  );
});

export default Main;

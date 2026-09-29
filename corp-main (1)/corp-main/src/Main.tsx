/* eslint-disable @stylistic/object-curly-newline */
import React, { FC, useEffect } from 'react';
import { AiAssistant } from '@sber-sbertransport/ai-agent';
import { observer } from 'mobx-react';
import { Spin } from 'antd';
import { use2GIS } from '@sber-sbertransport/ui-kit/src';
import { OrgStructureType } from '@sber-sbertransport/mf-core/dist/constants/constants';
import mfLoader from 'mf/MFLoader';
import { queryCache } from 'constants/constants';
import { Roles, FleetSingleRoles } from 'constants/constants.app';
import Layout from 'ui/Layout';
import SideMenu from 'ui/SideMenu/SideMenu';
import Header from 'ui/Header/Header';
import { useBreadcrumbs } from 'shared/components/Breadcrumbs';
import { useAppStore, StoreNames } from 'stores';
import { SettingsContext, defaultSettings as settings } from 'stores/SettingsContext';
import { OrganizationContext, useHook as useOrganization } from 'context/Organization.context';
import { AccessError } from 'components/AccessError/AccessError';
import { useVerifyUserAccess } from 'utils/useUserAccess';
import AppRouter from './AppRouter';
import { useProfile } from 'api/profile';
import { useRole } from 'utils/useRole';
import { useAPIQueryCache } from 'api';

const PlatformProvider = React.lazy(() => mfLoader(import('platform/AppProvider')));
const PassengersProvider = React.lazy(() => mfLoader(import('passengers/AppProvider')));
const CargoProvider = React.lazy(() => mfLoader(import('cargo/AppProvider')));
const FleetProvider = React.lazy(() => mfLoader(import('fleet/AppProvider')));
const PersonalInfoConsent = React.lazy(() => mfLoader(import('auth/PersonalInfoConsent')));

const Main: FC = (): JSX.Element | null => {
  const { http, logger } = useAppStore();

  const cache = useAPIQueryCache();
  const organization = useOrganization();
  const breadcrumbs = useBreadcrumbs();
  const roles = useRole();
  const mapContext = use2GIS();

  const { consent, orgStructureType, id } = useProfile().data;
  const hasSingleFleetRole = roles.length === 1 && FleetSingleRoles.includes(roles[0] as Roles);

  useEffect(() => {
    return () => {
      cache.removeQueries(['profile']);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    const updateProfile = () => {
      cache.refetchQueries(['profile']);
    };

    if (!consent) {
      window.addEventListener('privacyPolicyAgreed', updateProfile);
    }

    return () => {
      window.removeEventListener('privacyPolicyAgreed', updateProfile);
    };
  }, [consent, cache]);

  // То что идет в провайдеры МФ от Мейна
  const mfProviderValue = {
    queryCache,
    settings,
    organization,
    breadcrumbs,
    mapContext,
  };

  if (!hasSingleFleetRole && !consent && orgStructureType !== OrgStructureType.INTERNAL) return (
    <PersonalInfoConsent />
  );

  // Дриллинг МФ провайдеров - не лучшая идея, может есть смысл разделить по роутам!
  // но также возможно потребуется доступ в лейоут из одного из МФ. Поэтому пока так
  return (
    <SettingsContext.Provider value={settings}>
      <OrganizationContext.Provider value={organization}>
        <PlatformProvider value={mfProviderValue}>
          <PassengersProvider value={mfProviderValue}>
            <CargoProvider value={mfProviderValue}>
              <FleetProvider value={mfProviderValue}>
                <Layout headerContent={<Header />} drawerContent={<SideMenu />}>
                  <AppRouter />
                  <AiAssistant
                    agentType="WebCorp"
                    userId={id}
                    httpService={http}
                    onMessage={logger.toMessage}
                  />
                </Layout>
              </FleetProvider>
            </CargoProvider>
          </PassengersProvider>
        </PlatformProvider>
      </OrganizationContext.Provider>
    </SettingsContext.Provider>
  );
};

export default observer(() => {
  const { [StoreNames.authStore]: authStore } = useAppStore();
  const isUserAccess = useVerifyUserAccess();

  if (!authStore.token) return <Spin />;

  if (!isUserAccess) return <AccessError />;

  return <Main />;
});

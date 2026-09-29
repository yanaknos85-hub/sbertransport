import React, { FC } from 'react';
import { observer } from 'mobx-react';
import { Spin } from 'antd';
import { OrgStructureType } from '@sber-sbertransport/mf-core/dist/constants/constants';
import { useAppStore, StoreNames } from 'stores';
import Layout from 'ui/Layout';
import { OrganizationContext, useHook as useOrganization } from 'context/Organization.context';
import { SettingsContext, defaultSettings } from 'stores/SettingsContext';
import AppRouter from './AppRouter';
import { useProfile } from 'api/profile';
import MFLoader from 'mf/MFLoader';

const PersonalInfoConsent = React.lazy(() => MFLoader(import('auth/PersonalInfoConsent')));

const Main: FC = observer((): JSX.Element | null => {
  const organization = useOrganization();
  const { consent, orgStructureType } = useProfile().data;

  if (!consent && orgStructureType !== OrgStructureType.INTERNAL) return (
    <PersonalInfoConsent />
  );

  return (
    <SettingsContext.Provider value={defaultSettings}>
      <OrganizationContext.Provider value={organization}>
        <Layout>
          <AppRouter />
        </Layout>
      </OrganizationContext.Provider>
    </SettingsContext.Provider>
  );
});

const MainResolver = observer(() => {
  const { [StoreNames.authStore]: authStore } = useAppStore();

  if (!authStore.token) {
    return <Spin />;
  }

  return <Main />;
});

export default MainResolver;

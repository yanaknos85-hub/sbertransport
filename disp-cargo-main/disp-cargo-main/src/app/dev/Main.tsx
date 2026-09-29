/* eslint-disable @stylistic/jsx-one-expression-per-line */
import React, { FC, lazy } from 'react';
import { observer } from 'mobx-react';
import { Spin } from 'antd';
import { useAppStore, StoreNames } from 'ioc';
import Layout from 'ui/Layout';
import AppRouter from './AppRouter';
import { useProfile } from 'api/profile/profile.api';
import MFLoader from 'mf/MFLoader';
import Header from 'ui/Header/Header';
import SideMenu from 'ui/SideMenu/SideMenu';
import { TripsProvider } from 'context/Trips.context';
import { useRole } from 'hooks/useRole';
import { Roles } from 'constants/app.constants';
import { useAutopark, AutoparkContext } from 'context/Autopark.context';
import SpinWrapped from 'components/SpinWrapped/SpinWrapped';

const PersonalInfoConsent = lazy(() => MFLoader(import('auth/PersonalInfoConsent')));

const Main: FC = observer((): JSX.Element | null => {
  const { consent, contractorId } = useProfile().data;
  const roles = useRole();
  const isAdmin = roles.includes(Roles.ROLE_DISPATCHER_ROOM_ADMIN)
    || roles.includes(Roles.ROLE_FEDERAL_DISPATCHER_CONTRACTOR);

  if (!consent && !isAdmin) return (
    <PersonalInfoConsent disp />
  );

  const isLoading = isAdmin && !contractorId;

  return (
    <Layout headerContent={<Header />} drawerContent={<SideMenu />}>
      {isLoading ? <SpinWrapped /> : (
        <TripsProvider>
          <AppRouter />
        </TripsProvider>
      )}
    </Layout>
  );
});

const AutoparkProvider: FC = ({ children }) => {
  const autopark = useAutopark();

  return (
    <AutoparkContext.Provider value={autopark}>
      {children}
    </AutoparkContext.Provider>
  );
};

const MainResolver = observer(() => {
  const { [StoreNames.authStore]: authStore } = useAppStore();

  if (!authStore.token) {
    return <Spin />;
  }

  return (
    <AutoparkProvider>
      <Main />
    </AutoparkProvider>
  );
});

export default MainResolver;

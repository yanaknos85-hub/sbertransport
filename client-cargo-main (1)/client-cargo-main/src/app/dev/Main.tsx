import React, { FC, lazy, useEffect } from 'react';
import { OrgStructureType } from '@sber-sbertransport/mf-core/dist/constants/constants';
import AppRouter from 'AppRouter';
import { observer } from 'mobx-react';
import { SpinWrapped } from 'shared/components';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames, useInitStore } from 'stores';
import Layout from 'ui/Layout/Layout';

import { IS_REMOTE } from 'constants/constants.env';

const PersonalInfoConsent = lazy(() => import('auth/PersonalInfoConsent'));

export const Main: FC = observer(() => {
  // Set Default Store Values!
  const { isSelfEmployeeLoaded } = useInitStore();

  const {
    [StoreNames.selfStore]: selfStore,
    [StoreNames.authStore]: authStore,
  } = useAppStoreContext();

  useEffect(() => {
    if (!IS_REMOTE && authStore.isAuthenticated) {
      selfStore.initStore();
    }
  }, [authStore.isAuthenticated]);

  if (!isSelfEmployeeLoaded) {
    return <SpinWrapped />;
  }

  const { selfEmployee } = selfStore;

  const needConsent = !IS_REMOTE && !selfEmployee.consent && selfEmployee.orgStructureType !== OrgStructureType.INTERNAL;

  if (needConsent) {
    return <PersonalInfoConsent />;
  }

  return (
    <Layout>
      <AppRouter />
    </Layout>
  );
});

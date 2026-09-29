import React, { FC, lazy, useEffect } from 'react';
import { observer } from 'mobx-react';
import AppRouter from 'AppRouter';
import Layout from 'modules/EmployeeApp/ui/Layout';
import { StoreNames, hooks } from 'stores';
import { SpinWrapped } from 'shared/components';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { IS_REMOTE } from 'constants/constants.env';

const PersonalInfoConsent = lazy(() => import('auth/PersonalInfoConsent'));

export const Main: FC = observer(() => {
  const { isSelfEmployeeLoaded } = hooks.useInitStore();

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

  const needConsent = !IS_REMOTE && !selfEmployee.consent && selfEmployee.orgStructureType !== 'INTERNAL';

  if (needConsent) {
    return <PersonalInfoConsent />;
  }

  return (
    <Layout>
      <AppRouter />
    </Layout>
  );
});

import { FC, Suspense, useEffect } from 'react';
import { observer } from 'mobx-react';
import { Outlet } from 'react-router';
import { StoreNames } from 'stores/storeNames';
import { useAppStore } from 'stores/stores.context';
import { FCC } from 'types/global';
import Spin from 'components/Spin';
import { useProfile } from 'api/services/DispatcherRoom/DispatcherRoom.query';
import PersonalInfoConsent from '../modules/PersonalInfoConsent/PersonalInfoConsent';

const CheckConsent: FCC = ({ children }) => {
  const { data, refetch } = useProfile({
    keepPreviousData: true,
  });

  useEffect(() => {
    const updateProfile = () => {
      refetch();
    };

    if (!data.consent) {
      window.addEventListener('privacyPolicyAgreed', updateProfile);
    }

    return () => {
      window.removeEventListener('privacyPolicyAgreed', updateProfile);
    };
  }, [data.consent, refetch]);

  if (!data.consent) {
    return <PersonalInfoConsent />;
  }

  return children;
};

const ProtectedLayout: FC = observer(() => {
  const { [StoreNames.authStore]: authStore } = useAppStore();

  if (!authStore.isAuthenticated) return <Spin />;

  return (
    <Suspense fallback={<Spin />}>
      <CheckConsent>
        <Outlet />
      </CheckConsent>
    </Suspense>
  );
});

export default ProtectedLayout;

import { FC, useEffect } from 'react';
import { AutoCenter, SpinLoading } from 'antd-mobile';

import { useAppStore } from 'stores/stores.context';

const Logout: FC = () => {
  const { authStore } = useAppStore();

  useEffect(() => {
    authStore.logout();
  }, [authStore]);

  return (
    <AutoCenter>
      <SpinLoading />
    </AutoCenter>
  );
};

export default Logout;

import React, { FC } from 'react';
import { observer } from 'mobx-react';
import { Spin } from 'antd';
import { useAppStore, StoreNames } from 'ioc';
import AppRouter from './AppRouter';
import { useProfile } from 'api/profile/profile.api';

const Main: FC = () => {
  const { contractorId } = useProfile().data;

  if (!contractorId) {
    return <Spin />;
  }

  return <AppRouter />;
};

export default observer((): JSX.Element | null => {
  const {
    [StoreNames.authStore]: authStore,
  } = useAppStore();

  if (!authStore.token) {
    return <Spin />;
  }

  return <Main />;
});

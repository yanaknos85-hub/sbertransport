import React, { FC } from 'react';
import { observer } from 'mobx-react';
import { Spin } from 'antd';
import { useAppStore, StoreNames } from 'stores';
import AppRouter from './AppRouter';

const Main: FC = observer((): JSX.Element | null => {
  const {
    [StoreNames.authStore]: authStore,
  } = useAppStore();

  if (!authStore.token) {
    return <Spin />;
  }

  return <AppRouter />;
});

export default Main;

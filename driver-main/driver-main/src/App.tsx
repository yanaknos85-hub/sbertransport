
import { FC, useEffect } from 'react';
import { BrowserRouter } from 'react-router';
import { ConfigProvider, SafeArea } from 'antd-mobile';
import ruRu from 'antd-mobile/es/locales/ru-RU';
import { QueryClientProvider } from '@tanstack/react-query';
import dayjs from 'dayjs';
import utc from 'dayjs/plugin/utc';
import duration from 'dayjs/plugin/duration';
import { observer } from 'mobx-react';

import { queryClient } from 'api';
import { StoreNames } from 'stores/storeNames';
import { useAppStore } from 'stores/stores.context';
import { IS_BASIC_AUTH, IS_MOCKED_API, IS_MOCKED_AUTH } from 'constants/env.constants';
import AppRouter from 'AppRouter';
import InstallPWA from 'modules/InstallPWA/InstallPwa';

import 'reflect-metadata';

import 'react-virtualized/styles.css';
import './styles/sb-sans.css';
import './styles/redefinition-antd.scss';
import './styles/global.scss';

dayjs.extend(utc);
dayjs.extend(duration);

const App: FC = observer(() => {
  const {
    [StoreNames.authStore]: authStore,
    [StoreNames.configStore]: configStore,
  } = useAppStore();

  useEffect(() => {
    configStore.setConfig({
      isBasicAuth: IS_BASIC_AUTH,
      isMockedApi: IS_MOCKED_API,
      isMockedAuth: IS_MOCKED_AUTH,
    });

    authStore.check();
  }, [authStore, configStore]);

  return (
    <BrowserRouter>
      <ConfigProvider locale={ruRu}>
        <QueryClientProvider client={queryClient}>
          <InstallPWA />
          <AppRouter />
          <SafeArea position="bottom" className="st-safe-area" />
        </QueryClientProvider>
      </ConfigProvider>
    </BrowserRouter>
  );
});

export default App;

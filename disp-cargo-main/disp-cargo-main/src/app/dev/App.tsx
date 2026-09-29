import { observer } from 'mobx-react';
import React, { Suspense } from 'react';
import { Router } from 'react-router-dom';
import moment from 'moment';
import 'moment/locale/ru';
import 'reflect-metadata';
import ErrorBoundary from 'components/ErrorBoundary';
import SpinWrapped from 'components/SpinWrapped/SpinWrapped';
import { IS_REMOTE } from 'constants/env.constants';
import { RouterProvider } from 'context/Router.context';
import { useAppStore, StoreNames } from 'ioc';
import AuthRouter from './AuthRouter';
import useClientType from 'hooks/useClientType';
import useParamsSerializer from 'hooks/useParamsSerializer';
import { useTestMode } from 'hooks/useTestMode';

moment().locale('ru');

if (!IS_REMOTE) {
  import('../../styles/sb-sans.css');
  import('../../styles/style.less');
}

const App = observer((): JSX.Element => {
  const {
    [StoreNames.configStore]: { history },
  } = useAppStore();

  useTestMode();
  useClientType();
  useParamsSerializer();

  return (
    <ErrorBoundary>
      <Suspense fallback={<SpinWrapped />}>
        <Router history={history}>
          <RouterProvider>
            <AuthRouter />
          </RouterProvider>
        </Router>
      </Suspense>
    </ErrorBoundary>
  );
});

export default App;

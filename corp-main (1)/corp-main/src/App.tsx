import { observer } from 'mobx-react';
import React, { Suspense } from 'react';
import { Router } from 'react-router-dom';
import moment from 'moment';
import 'moment/locale/ru';
import 'reflect-metadata';
import { RouterProvider } from 'context/Router.context';
import ErrorBoundary from 'shared/components/ErrorBoundary/ErrorBoundary';
import SpinWrapped from 'shared/components/SpinWrapped/SpinWrapped';
import { IS_REMOTE } from './constants/constants.env';
import { useAppStore, StoreNames } from 'stores';
import AuthRouter from './AuthRouter';
import useClientType from 'shared/hooks/useClientType';

moment().locale('ru');

if (!IS_REMOTE) {
  import('./styles/sb-sans.css');
  import('./styles/style.less');
}

const App = observer((): JSX.Element => {
  const {
    [StoreNames.configStore]: { history },
  } = useAppStore();

  useClientType();

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

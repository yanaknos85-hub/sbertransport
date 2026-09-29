import 'moment/locale/ru';
import 'reflect-metadata';

import React, { Suspense } from 'react';
import { Router } from 'react-router-dom';
import { useHistory as History } from '@sber-sbertransport/mf-core';
import { observer } from 'mobx-react';
import moment from 'moment';
import { SpinWrapped } from 'shared/components';
import ErrorBoundary from 'shared/components/ErrorBoundary';

import { RouterProvider } from 'context/Router.context';

import { IS_REMOTE } from '../../constants/constants.env';
import AuthRouter from './AuthRouter';

moment().locale('ru');

if (!IS_REMOTE) {
  import('font-awesome/css/font-awesome.min.css');
  import('@sber-sbertransport/ui-kit/static/style.less');
}

export const history = History();

const App = observer((): JSX.Element => {
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

import { observer } from 'mobx-react';
import React, { Suspense } from 'react';
import { Router, Route, Switch } from 'react-router-dom';
import moment from 'moment';
import 'moment/locale/ru';
import 'reflect-metadata';
import { RouterProvider } from 'context/Router.context';
import ErrorBoundary from 'shared/components/ErrorBoundary/ErrorBoundary';
import SpinWrapped from 'shared/components/SpinWrapped/SpinWrapped';
import { useAppStore, StoreNames } from 'stores';
import * as routes from 'constants/constants.routes';
import AuthRouter from './AuthRouter';
import MobileAppRef from './MobileAppRef';

moment().locale('ru');

import 'font-awesome/css/font-awesome.min.css';
// import './styles/style.less';
import './styles/styles.scss';

import '@sber-sbertransport/ui-kit/static';

const App = observer((): JSX.Element => {
  const {
    [StoreNames.configStore]: { history },
  } = useAppStore();

  return (
    <ErrorBoundary>
      <Suspense fallback={<SpinWrapped />}>
        <Router history={history}>
          <RouterProvider>
            <Switch>
              <Route
                exact
                path={routes.MOBILE_APP}
                component={MobileAppRef}
              />
              <Route path="*" component={AuthRouter} />
            </Switch>
          </RouterProvider>
        </Router>
      </Suspense>
    </ErrorBoundary>
  );
});

export default App;

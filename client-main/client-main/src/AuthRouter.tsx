/* eslint-disable @stylistic/jsx-max-props-per-line */
import { observer } from 'mobx-react';
import React, { FC, useEffect, useRef } from 'react';
import { Route, Switch, Redirect } from 'react-router-dom';
import mfLoader from 'mf/MFLoader';
import {
  IS_BASIC_AUTH,
  IS_MOCKED_AUTH,
  IS_MOCKED_API,
  IS_REMOTE
} from 'constants/constants.env';
import * as routes from 'constants/constants.routes';
import { useAppStore, StoreNames } from 'stores';
import Main from './Main';

const Auth = React.lazy(() => mfLoader(import('auth/App')));
const AuthProvider = React.lazy(() => mfLoader(import('auth/AppProvider')));

export const AuthRouter: FC = observer((): JSX.Element => {
  const {
    [StoreNames.authStore]: authStore,
    [StoreNames.selfStore]: selfStore,
    [StoreNames.configStore]: configStore,
  } = useAppStore();

  const IS_SDO = configStore.env.IS_SDO;

  const url = useRef(new URL(window.location.href));
  const refreshToken = url.current.searchParams.get('refreshToken');

  useEffect(() => {
    configStore.setConfig({
      isBasicAuth: IS_BASIC_AUTH,
      isMockedApi: IS_MOCKED_API,
      isMockedAuth: IS_MOCKED_AUTH,
    });

    if (!IS_REMOTE && Object.keys(authStore || {}).length) {
      if (IS_SDO && refreshToken) {
        window.sessionStorage.setItem('refreshToken', refreshToken);
      }

      authStore.check();
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (authStore.isAuthenticated) {
      selfStore.initStore();
    }
  }, [authStore.isAuthenticated]);

  return (
    <Switch>
      <Route
        path={routes.AUTH}
        component={() => (
          <AuthProvider value={{ settings: {} }}>
            <Auth />
          </AuthProvider>
        )}
      />
      <Route path={`${routes.MAIN}`} component={Main} />
      <Route exact path="/" component={() => <Redirect to={routes.MAIN} />} />
      <Route path="*" component={() => <Redirect to={routes.PAGE_404} />} />
    </Switch>
  );
});

export default AuthRouter;

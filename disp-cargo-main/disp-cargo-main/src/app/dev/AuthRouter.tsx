/* eslint-disable @stylistic/jsx-max-props-per-line */
import { observer } from 'mobx-react';
import React, { FC, useEffect, lazy } from 'react';
import { Route, Switch, Redirect } from 'react-router-dom';
import {
  IS_BASIC_AUTH,
  IS_MOCKED_API,
  IS_MOCKED_AUTH,
  IS_REMOTE
} from 'constants/env.constants';
import * as routes from 'constants/routes.constants';
import { useAppStore, StoreNames } from 'ioc';
import mfLoader from 'mf/MFLoader';
import Main from './Main';

const Auth = lazy(() => mfLoader(import('auth/App')));
const AuthProvider = lazy(() => mfLoader(import('auth/AppProvider')));

const authSettings = {
  settings: {
    hideSupport: true,
    dispDesign: true,
  },
};

export const AuthRouter: FC = observer((): JSX.Element => {
  const {
    [StoreNames.authStore]: authStore,
    [StoreNames.configStore]: configStore,
  } = useAppStore();

  useEffect(() => {
    if (!IS_REMOTE) {
      configStore.setConfig({
        isBasicAuth: IS_BASIC_AUTH,
        isMockedApi: IS_MOCKED_API,
        isMockedAuth: IS_MOCKED_AUTH,
        isCorp: true,
      });

      if (Object.keys(authStore || {}).length) {
        authStore.check();
      }
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <Switch>
      {!IS_REMOTE && (
        <Route
          path={routes.AUTH}
          component={() => (
            <AuthProvider value={authSettings}>
              <Auth />
            </AuthProvider>
          )}
        />
      )}
      <Route path={routes.APP} component={Main} />
      <Route path="/" component={Main} />
      <Route exact path="/" component={() => <Redirect to={routes.APP} />} />
      <Route path="*" component={() => <Redirect to={routes.PAGE_404} />} />
    </Switch>
  );
});

export default AuthRouter;

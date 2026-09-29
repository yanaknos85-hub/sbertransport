import React, { FC, ReactNode, useEffect } from 'react';
import { Redirect, Route, Switch } from 'react-router-dom';
import mfLoader from 'mf/MFLoader';
import { observer } from 'mobx-react';
import { StoreNames, useAppStore } from 'stores';

import {
  IS_BASIC_AUTH, IS_MOCKED_API, IS_MOCKED_AUTH, IS_REMOTE
} from 'constants/constants.env';
import * as routes from 'constants/constants.routes';

import { Main } from './Main';

const Auth = React.lazy(() => mfLoader(import('auth/App')));
const AuthProvider = React.lazy(() => mfLoader(import('auth/AppProvider')));

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
          <AuthProvider value={{ settings: {} }}>
            <Auth />
          </AuthProvider>
        )}
      />
      )}

      <Route path="/" component={Main} />
      <Route render={(): ReactNode => <Redirect to={routes.CARGO_CREATE} />} />
    </Switch>
  );
});

export default AuthRouter;

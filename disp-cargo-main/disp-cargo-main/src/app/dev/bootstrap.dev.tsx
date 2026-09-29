import React, { Suspense, lazy } from 'react';
import ReactDOM from 'react-dom';
import { error } from 'mf/debug';
import { IS_REMOTE } from 'constants/env.constants';
import SpinWrapped from 'components/SpinWrapped/SpinWrapped';

const App = lazy(() => import('./App'));
const AppProvider = lazy(() => import('./AppProvider'));
const bootAuthProviders = () => import('auth/Providers');

if (!IS_REMOTE) {
  bootAuthProviders()
    .then(({ default: initAuthProviders }) => {
      initAuthProviders();
    })
    .then(() => {
      ReactDOM.render((
        <Suspense fallback={<SpinWrapped />}>
          <AppProvider>
            <App />
          </AppProvider>
        </Suspense>
      ), document.querySelector('#root'));
    })
    .catch(error);
}

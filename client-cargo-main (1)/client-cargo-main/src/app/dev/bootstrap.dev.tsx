import React, { Suspense } from 'react';
import ReactDOM from 'react-dom';
import { error } from 'mf/debug';
import ErrorBoundary from 'shared/components/ErrorBoundary';
import SpinWrapped from 'shared/components/SpinWrapped';

import { IS_REMOTE } from 'constants/constants.env';

const App = React.lazy(() => import('./App'));
const AppProvider = React.lazy(() => import('./AppProvider'));
const bootAuthProviders = () => import('auth/Providers');

if (!IS_REMOTE) {
  bootAuthProviders()
    .then(({ default: initAuthProviders }) => {
      initAuthProviders();
    })
    .then(() => {
      ReactDOM.render((
        <ErrorBoundary>
          <Suspense fallback={<SpinWrapped />}>
            <AppProvider>
              <App />
            </AppProvider>
          </Suspense>
        </ErrorBoundary>
      ), document.querySelector('#root'));
    })
    .catch(error);
}

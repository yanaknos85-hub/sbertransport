import React, { Suspense } from 'react';
import ReactDOM from 'react-dom';
import { error } from 'mf/debug';
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
        <Suspense fallback={null}>
          <AppProvider>
            <App />
          </AppProvider>
        </Suspense>
      ), document.querySelector('#root'));
    })
    .catch(error);
}

import { createRoot } from 'react-dom/client';
import App from 'App';
import { initRootStore, rootContainer } from 'stores/stores';
import { AppStoreContext } from 'stores/stores.context';

const rootElement = document.getElementById('root');

if (!rootElement) {
  throw new Error('Element with id #root doesn\'t exist');
}

const root = createRoot(rootElement!);

const appStores = initRootStore(rootContainer);

root.render(
  <AppStoreContext.Provider value={appStores}>
    <App />
  </AppStoreContext.Provider>
);

import { logFatalError, useAppStoreContext as useGeneralStoreContext } from '@sber-sbertransport/mf-core';
import { Container } from 'inversify';
import { ERR } from './ioc.errors';
import initAppStore, { IAppStore } from './ioc.stores';

export default function AppStore(): IAppStore {
  const generalContextStore = useGeneralStoreContext();

  if (!generalContextStore?.rootContainer) {
    logFatalError(ERR.CONTEXT);
  }

  const container: Container = new Container({
    defaultScope: 'Singleton',
    autoBindInjectable: true,
  });

  container.parent = generalContextStore.rootContainer;

  const store = { ...generalContextStore.init(), ...initAppStore(container) };

  return store;
}

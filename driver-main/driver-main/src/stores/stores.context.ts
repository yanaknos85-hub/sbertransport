import { createContext, useContext } from 'react';
import { IRootStore } from './stores.types';

export const AppStoreContext = createContext<IRootStore | null>(null);

export const useAppStore = () => {
  const appStore = useContext(AppStoreContext);

  if (!appStore) {
    throw new Error('useAppStore must be inside AppStoreContext provider');
  }

  return appStore;
};

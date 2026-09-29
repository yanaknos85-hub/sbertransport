import { logFatalError } from '@sber-sbertransport/mf-core';
import React from 'react';
import { APP_NAME } from 'constants/constants.env';
import { IAppStore } from './ioc.stores';

export const AppStoreContext = React.createContext<IAppStore | null>(null);

export const useAppStore: () => IAppStore = (): IAppStore => {
  const context = React.useContext(AppStoreContext);

  if (!context) {
    logFatalError(`MF <${APP_NAME}> AppStoreContext usage before initiation.`);
  }

  return context as IAppStore;
};

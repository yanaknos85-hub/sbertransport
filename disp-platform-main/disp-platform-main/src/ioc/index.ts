import AppStore from 'ioc/ioc.container';
import { AppStoreContext, useAppStore } from 'ioc/ioc.context';
import { StoreNames } from 'ioc/ioc.storeNames';
import initAppStore, { IAppStore as _IAppStore } from 'ioc/ioc.stores';
import { TYPES } from 'ioc/ioc.types';

export type IAppStore = _IAppStore;

export {
  TYPES,
  initAppStore,
  AppStoreContext,
  useAppStore,
  StoreNames
};

export default AppStore;

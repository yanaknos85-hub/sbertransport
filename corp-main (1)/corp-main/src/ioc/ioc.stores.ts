/* eslint-disable @typescript-eslint/no-unused-vars */
import { IRootStore } from '@sber-sbertransport/mf-core';
import { interfaces } from 'inversify';

import { StoreNames } from './ioc.storeNames';
import { TYPES } from './ioc.types';
import { DIHomeStore } from 'stores/Home/DIHome.store';
import { IHomeService, IHomeStore } from 'stores/Home/Home.interface';
import { DIHomeService } from 'stores/Home/DIHome.service';

export type IAppStore = {
  [StoreNames.homeStore ]: DIHomeStore;
} & IRootStore;

export default function initAppStore(container: interfaces.Container): IAppStore {
  container.bind<IHomeService>(TYPES.IHomeService).to(DIHomeService);
  container.bind<DIHomeStore>(TYPES.IHomeStore).to(DIHomeStore);

  return {
    [StoreNames.homeStore]: container.get<DIHomeStore>(TYPES.IHomeStore),
  } as IAppStore;
}

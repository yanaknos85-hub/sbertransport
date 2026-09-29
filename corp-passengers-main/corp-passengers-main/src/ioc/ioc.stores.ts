import { IRootStore } from '@sber-sbertransport/mf-core';
import { interfaces } from 'inversify';

import { StoreNames } from './ioc.storeNames';
import { TYPES } from './ioc.types';

import { IPassengerStore, IPassengerService } from 'modules/OrderExecution/interfaces/Passengers/Passenger.interface';
import { DIPassengerStore } from 'modules/OrderExecution/stores/Passengers/DIPassengerService.store';
import { DIPassengerService } from 'modules/OrderExecution/stores/Passengers/DIPassengerService.service';
import { DIRegistryStore } from 'stores/Registry/DIRegistry.store';
import { IRegistryService, IRegistryStore } from 'stores/Registry/Registry.interface';
import { DITariffsStore } from 'stores/Tariffs/DITariffs.store';
import { DITariffsService } from 'stores/Tariffs/DITariffs.service';
import { DIRegistryService } from 'stores/Registry/DIRegistry.service';
import { ITariffsService, ITariffsStore } from 'stores/Tariffs/Tariffs.interface';

export type IAppStore = {
  [StoreNames.passengerStore]: IPassengerStore;
  [StoreNames.registryStore]: DIRegistryStore;
  [StoreNames.tariffsStore]: ITariffsStore;
} & IRootStore;

export default function initAppStore(container: interfaces.Container): IAppStore {
  container.bind<IPassengerStore>(TYPES.IPassengerStore).to(DIPassengerStore);
  container.bind<IPassengerService>(TYPES.IPassengerService).to(DIPassengerService);
  container.bind<IRegistryStore>(TYPES.IRegistryStore).to(DIRegistryStore);
  container.bind<ITariffsStore>(TYPES.ITariffsStore).to(DITariffsStore);
  container.bind<ITariffsService>(TYPES.ITariffsService).to(DITariffsService);
  container.bind<IRegistryService>(TYPES.IRegistryService).to(DIRegistryService);

  return {
    passengerStore: container.get<IPassengerStore>(TYPES.IPassengerStore),
    registryStore: container.get<IRegistryStore>(TYPES.IRegistryStore),
    tariffsStore: container.get<ITariffsStore>(TYPES.ITariffsStore),
  } as IAppStore;
}

import { IRootStore } from '@sber-sbertransport/mf-core';
import { interfaces } from 'inversify';

import { StoreNames } from './ioc.storeNames';
import { TYPES } from './ioc.types';

import { ICargoStore, ICargoService } from 'modules/OrderExecution/interfaces/Cargo/Cargo.interface';

import { DICargoStore } from 'modules/OrderExecution/stores/Cargo/DICargoStore.store';
import { DICargoService } from 'modules/OrderExecution/stores/Cargo/DICargoService.service';

import { IPlannerService, IPlannerStore } from 'stores/Planner/Planner.interface';
import { DIPlannerStore } from 'stores/Planner/DIPlanner.store';
import { DIPlannerService } from 'stores/Planner/DIPlanner.service';

export type IAppStore = {
  [StoreNames.cargoStore]: ICargoStore;
  [StoreNames.plannerStore]: IPlannerStore;
} & IRootStore;

export default function initAppStore(container: interfaces.Container): IAppStore {
  container.bind<ICargoStore>(TYPES.ICargoStore).to(DICargoStore);
  container.bind<ICargoService>(TYPES.ICargoService).to(DICargoService);

  container.bind<IPlannerStore>(TYPES.IPlannerStore).to(DIPlannerStore);
  container.bind<IPlannerService>(TYPES.IPlannerService).to(DIPlannerService);

  return {
    cargoStore: container.get<ICargoStore>(TYPES.ICargoStore),
    plannerStore: container.get<IPlannerStore>(TYPES.IPlannerStore),
  } as IAppStore;
}

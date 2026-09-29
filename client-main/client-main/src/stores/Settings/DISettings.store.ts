import type { ILogger, ISelfEmployeeStore } from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';
import { action, computed, observable } from 'mobx';

import { DesignVersion } from 'constants/constants.app';

import { TYPES } from 'ioc/types';

import type { ITripStore } from '../Trip/Trip.interface';
import { ISettingsStore } from './Settings.interface';

@injectable()
export class DISettingsStore implements ISettingsStore {
  @inject(TYPES.ITripStore)
    tripStore!: ITripStore;

  @inject(TYPES.ISelfEmployeeStore)
    selfStore!: ISelfEmployeeStore;

  @inject(TYPES.ILogger)
    logger!: ILogger;

  @observable
    isUploaderVisible = false;

  @observable
  readonly designVersion: DesignVersion = DesignVersion.Middle;

  @computed
  get menuCounterList(): Record<string, number> {
    const plannedTripsLength
      // Убрал по требованиям бизнеса на этой неделе :)
      // .filter(filterByAuthor)
      = this.tripStore.nonTerminalTotalElements;

    return {
      ['trips']: plannedTripsLength ?? 0,
    };
  }

  @action.bound
  setIsUploaderVisible(val: boolean): void {
    this.isUploaderVisible = val;
  }
}

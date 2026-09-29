/* eslint-disable @typescript-eslint/no-unused-vars */
import { IRootStore } from '@sber-sbertransport/mf-core';
import { interfaces } from 'inversify';

// eslint-disable-next-line @typescript-eslint/ban-types
export type IAppStore = {

} & IRootStore;

export default function initAppStore(container: interfaces.Container): IAppStore {
  return {
  } as IAppStore;
}

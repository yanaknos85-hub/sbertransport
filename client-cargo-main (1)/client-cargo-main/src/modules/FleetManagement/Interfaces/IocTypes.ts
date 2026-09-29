import { interfaces } from 'inversify/lib/interfaces/interfaces';

export const Types = {
  IParkingsService: Symbol.for('IParkingsService'),
  IParkingsStore: Symbol.for('IParkingsStore'),
};

export type Container = interfaces.Container;

export enum StoreNames {
  parkingsStore = 'parkingsStore',
}

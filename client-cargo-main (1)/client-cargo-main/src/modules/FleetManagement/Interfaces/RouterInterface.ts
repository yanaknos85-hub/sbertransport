import { Ioc } from './index';

export type TGetFullLinks = (appPath: string) => string[];

export type TGetStores = (container: Ioc.Container) => object;

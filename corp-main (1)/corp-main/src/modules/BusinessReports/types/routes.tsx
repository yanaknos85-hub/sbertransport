import { ReactNode } from 'react';

export interface OneLevelRoute {
  title: string;
  route: string;
  component: ReactNode;
  tabs?: never;
}

export interface TwoLevelRoutes {
  title: string;
  route: string;
  component?: never;
  tabs: OneLevelRoute[];
  defaultRoute?: string;
}

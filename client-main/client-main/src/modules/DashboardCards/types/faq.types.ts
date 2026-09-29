import type { ReactNode } from 'react';
import { ServiceType } from '../constants/general.constants';

export interface ICollapseItem {
  label: string;
  children: ReactNode;
}

export type TCollapseItemsByService = Partial<Record<ServiceType, ICollapseItem[]>>;

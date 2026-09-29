import { FC } from 'react';

export interface MenuType {
  path: string;
  isExpanded: boolean;
  isSubPage: () => boolean;
}

export type TMenu = FC<MenuType>;

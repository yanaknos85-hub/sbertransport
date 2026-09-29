import { NavLink } from 'react-router-dom';

export type MenuItemType = {
  icon?: JSX.Element;
  $hasInnerChilds?: boolean;
  isOpen?: boolean;
  $isExpanded?: boolean;
  $isSubMenu?: boolean;
  $disable?: boolean;
  onClick?(e: React.MouseEvent<HTMLElement, MouseEvent>): void;
} & React.ComponentProps<typeof NavLink>;

export interface OnlySpoilerType {
  setOpen: React.Dispatch<React.SetStateAction<boolean>>;
  open: boolean;
  children: React.ReactNode;
  $isExpanded: boolean;
}

import { FunctionComponent } from 'enzyme';
import { Dispatch, SVGProps, SetStateAction } from 'react';
import { NavLink } from 'react-router-dom';

export type MenuItemType = {
  icon?: JSX.Element | FunctionComponent<SVGProps<SVGSVGElement> & { title?: string }> | string;
  $hasInnerChilds?: boolean;
  isOpen?: boolean;
  $isExpanded?: boolean;
  $isSubMenu?: boolean;
  $disable?: boolean;
  onClick?(e: React.MouseEvent<HTMLElement, MouseEvent>): void;
  setOpen?: React.Dispatch<React.SetStateAction<boolean>>;
  open?: boolean;
  handleOpen?: () => void;
  nonSpoiler?: boolean;
} & React.ComponentProps<typeof NavLink>;

export interface OnlySpoilerType {
  setOpen: React.Dispatch<React.SetStateAction<boolean>>;
  open: boolean;
  children: React.ReactNode;
  $isExpanded: boolean;
  handleOpen?: () => void;
  setMenuItems?: Dispatch<SetStateAction<any[]>>;
  typeMenu?: any[];
  titleMenu?: string;
  setTitleTypeMenuItems?: Dispatch<SetStateAction<string>>;
  isActive?: () => boolean;
}

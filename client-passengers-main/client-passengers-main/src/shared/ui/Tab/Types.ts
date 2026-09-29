import { SizeKind } from './StyledTab';

export interface TabProps {
  size?: SizeKind;
  isActive?: boolean;
  isPositive?: boolean;
  isReadOnly?: boolean;
  column?: boolean;
}

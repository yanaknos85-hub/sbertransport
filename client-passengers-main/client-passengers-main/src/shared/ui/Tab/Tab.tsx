import * as React from 'react';
import StyledTab, { SizeKind } from './StyledTab';
import { TabProps } from './Types';

const TTab: React.FC<TabProps & React.HTMLAttributes<HTMLSpanElement>> = ({
  isActive,
  size = SizeKind.M,
  style,
  onClick,
  children,
  isPositive = true,
  isReadOnly = false,
  column = true,
}) => {
  return (
    <StyledTab
      isActive={isActive}
      isPositive={isPositive}
      isReadOnly={isReadOnly}
      size={size}
      style={style}
      onClick={onClick}
      column={column}
    >
      {children}
    </StyledTab>
  );
};

export default TTab;

import * as React from 'react';
import StyledBar from './StyledBar';
import { BarProps, OrientationKind } from './Types';

const TBar: React.FC<BarProps & React.HTMLAttributes<HTMLDivElement>> = ({
  orientation = OrientationKind.Horizontal, style, children,
}) => {
  return (
    <StyledBar
      orientation={orientation}
      style={style}
    >
      { children }
    </StyledBar>
  );
};

export default TBar;

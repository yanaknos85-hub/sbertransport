import React from 'react';
import Popover from 'antd-mobile/es/components/popover';
import cn from 'classnames';

import { ITooltip } from './ITooltip';
import styles from './Tooltip.module.scss';

const Tooltip: React.FC<ITooltip> = ({
  className,
  children,
  trigger = 'click',
  mode = 'dark',
  arrow = true,
  ...props
}) => (
  <Popover
    {...props}
    trigger={trigger}
    mode={mode}
    className={cn(
      styles.tooltip,
      {
        [styles.arrowHide]: !arrow,
      },
      [className]
    )}
  >
    {children}
  </Popover>
);

export default Tooltip;

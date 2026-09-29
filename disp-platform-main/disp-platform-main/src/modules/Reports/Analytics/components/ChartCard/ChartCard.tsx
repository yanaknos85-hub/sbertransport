import React, { FC, ReactNode } from 'react';
import { Tooltip, TooltipProps } from 'antd';
import cn from 'classnames';

import HelpIcon from 'assets/icons/help.svg';
import styles from './ChartCard.module.scss';

export interface ChartCardProps {
  title: string;
  tooltipProps?: TooltipProps;
  actions?: ReactNode;
  className?: string;
}

const ChartCard: FC<ChartCardProps> = ({
  title, tooltipProps, actions, children, className,
}) => {
  return (
    <div className={cn(styles.container, className)}>
      <div className={styles.header}>
        <div className={styles.title}>
          <p>{title}</p>
          {tooltipProps && (
            <div className={styles.tooltipWrapper}>
              <Tooltip
                {...tooltipProps}
                getPopupContainer={trigger => trigger.parentNode as HTMLElement}
              >
                <img src={HelpIcon} alt="help" />
              </Tooltip>
            </div>
          )}
        </div>
        {actions && <div>{actions}</div>}
      </div>
      <div className={styles.content}>
        {children}
      </div>
    </div>
  );
};

export default ChartCard;

import React, { ComponentProps, FC } from 'react';
import { Switch } from 'antd';
import cn from 'classnames';
import styles from './index.module.scss';

interface SwitcherProps extends ComponentProps<typeof Switch> {
  title?: string;
  containerClassname?: string;
}

export const Switcher: FC<SwitcherProps> = ({
  title, containerClassname, ...props
}) => (
  <div className={cn(styles.switcher, containerClassname)}>
    <Switch {...props} />
    <span>{title}</span>
  </div>
);

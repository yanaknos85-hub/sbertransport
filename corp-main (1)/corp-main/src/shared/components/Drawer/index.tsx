import React, { FC } from 'react';
import { Drawer as AntdDrawer } from 'antd';
import { DrawerProps } from 'antd/lib/drawer';
import cn from 'classnames';
import { ReactComponent as CloseIcon } from 'shared/assets/svg/Close.svg';
import styles from './Drawer.module.scss';

export const Drawer: FC<DrawerProps> = props => (
  <AntdDrawer
    {...props}
    className={cn(styles.drawer, props.className)}
    closeIcon={<CloseIcon />}
  />
);

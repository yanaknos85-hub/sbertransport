import React, { FC } from 'react';
import { Tabs as AntdTabs } from 'antd';
import { TabsProps, TabPaneProps } from 'antd/lib/tabs';
import cn from 'classnames';
import styles from './index.module.scss';

export const Tabs: FC<TabsProps> = ({ className, ...props }) => (
  <AntdTabs {...props} className={cn(styles.tabs, className)} />
);

export const TabPane: FC<TabPaneProps> = props => <AntdTabs.TabPane {...props} />;

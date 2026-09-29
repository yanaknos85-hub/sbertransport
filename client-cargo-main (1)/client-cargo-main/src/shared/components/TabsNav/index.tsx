import React, { FC } from 'react';
import { useHistory as History } from '@sber-sbertransport/mf-core';
import { Tabs } from 'antd';

import { UseTabsNavProps } from './useTabsNavProps';

import styles from './styles.module.scss';

export interface TabsNavOption {
  label: React.ReactNode;
  key: string;
}

export interface TabsNavProps {
  defaultActiveTab: TabsNavOption;
  currentKey?: string;
  options: TabsNavOption[];
  onTabClick?(arg: string): void;
}

export const TabsNav: FC<TabsNavProps & UseTabsNavProps> = ({
  currentKey,
  options = [],
  defaultActiveTab,
  onTabClick,
}) => {
  const history = History();

  const clickHandler = (val: string): void => {
    if (onTabClick) {
      onTabClick(val);
      history.push(`./${val}`);
    } else {
      history.push(`./${val}`);
    }
  };

  return (
    <Tabs
      className={styles.tabs}
      activeKey={currentKey}
      defaultActiveKey={defaultActiveTab.key || ''}
      onChange={clickHandler}
    >
      {options.map(option => (
        <Tabs.TabPane tab={option.label} key={option.key} />
      ))}
    </Tabs>
  );
};

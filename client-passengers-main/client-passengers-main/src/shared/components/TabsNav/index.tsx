import { Tabs } from 'antd';
import React, { FC } from 'react';
import { useHistory as History } from '@sber-sbertransport/mf-core';

import { UseTabsNavProps } from './useTabsNavProps';

import styles from './styles.module.scss';

const { TabPane } = Tabs;

export interface TabsNavOption {
  label: string;
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
      className={styles.wrapperTabPane}
      activeKey={currentKey}
      defaultActiveKey={defaultActiveTab.key || ''}
      onChange={clickHandler}
    >
      {options.map(option => {
        return (
          <TabPane tab={<span className={styles.wrapperTab}>{option.label}</span>} key={option.key} />
        );
      })}
    </Tabs>
  );
};

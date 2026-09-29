import { Tabs } from 'antd';
import React, { FC } from 'react';
import { useHistory } from 'react-router-dom';

import { UseTabsNavProps } from './useTabsNavProps';

export interface TabsNavOption {
  label: string;
  key: string;
}

export interface TabsNavProps {
  defaultActiveTab: TabsNavOption;
  currentKey?: string;
  options: TabsNavOption[];
  onTabClick?(arg: string): void;
  onChange?: React.Dispatch<React.SetStateAction<string>>;
}

export const TabsNav: FC<TabsNavProps & UseTabsNavProps> = ({
  currentKey,
  options = [],
  defaultActiveTab,
  onTabClick,
  onChange,
}) => {
  const history = useHistory();

  const clickHandler = (val: string): void => {
    if (onChange) {
      return onChange(val);
    }

    if (onTabClick) {
      onTabClick(val);
      history.push(`./${val}`);
    } else {
      history.push(`./${val}`);
    }
  };

  return (
    <Tabs
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

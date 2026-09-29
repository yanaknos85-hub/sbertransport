import React, { FC } from 'react';
import { Tabs } from 'antd';

import { filters as TabsNames } from './types';

import styles from './styles.module.scss';

interface Props {
  onChange: (value: string) => void;
  isRegular?: boolean;
  tabFilterName: string;
}

export const CargoTabFilters: FC<Props> = props => {
  const {
    onChange, isRegular, tabFilterName,
  } = props;

  const tabsOptions = !isRegular ? Object.entries(TabsNames)
    .map(([name, value]: string[]) => {
      return isRegular ? { label: value, key: `${name}Regular` } : { label: value, key: name };
    }) : [];

  return (
    <Tabs
      className={styles.tabs}
      activeKey={tabFilterName}
      onChange={onChange}
    >
      {tabsOptions.map(option => (
        <Tabs.TabPane tab={option.label} key={option.key} />
      ))}
    </Tabs>
  );
};

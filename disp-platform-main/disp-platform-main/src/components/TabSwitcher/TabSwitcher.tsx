import React, { FC } from 'react';
import cn from 'classnames';

import styles from './tab-switcher.module.scss';

export interface Tab {
  key: string | number;
  label: string;
}

interface TabSwitcherProps {
  tabs: Tab[];
  value?: string | number;
  onChange?: (value: string | number) => void;
}

const TabSwitcher: FC<TabSwitcherProps> = ({
  tabs,
  value,
  onChange,
}) => (
  <div className={styles.tabSwitcher}>
    {tabs.map(tab => (
      <div
        key={tab.key}
        onClick={() => onChange?.(tab.key)}
        className={cn(styles.type, { [styles.selected]: tab.key === value })}
      >
        {tab.label}
      </div>
    ))}
  </div>
);

export default TabSwitcher;

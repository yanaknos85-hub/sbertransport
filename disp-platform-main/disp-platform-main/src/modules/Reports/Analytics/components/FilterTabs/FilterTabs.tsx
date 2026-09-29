import React, { useState } from 'react';
import cn from 'classnames';

import styles from './FilterTabs.module.scss';

interface TabItem {
  id: string;
  label: string;
}

interface FilterTabsProps {
  tabs: TabItem[];
  value?: string;
  onChange: (tabId: string) => void;
}

const FilterTabs = ({
  tabs, value, onChange,
}: FilterTabsProps) => {
  const [internalActiveTab, setInternalActiveTab] = useState<string>(tabs[0]?.id || '');

  const activeTab = value !== undefined ? value : internalActiveTab;

  const handleTabClick = (tabId: string) => {
    if (!value) {
      setInternalActiveTab(tabId);
    }

    onChange(tabId);
  };

  return (
    <div className={styles.controls}>
      {tabs.map(tab => (
        <span
          key={tab.id}
          className={cn(styles.item, {
            [styles.item_active]: activeTab === tab.id,
          })}
          onClick={() => handleTabClick(tab.id)}
        >
          {tab.label}
        </span>
      ))}
    </div>
  );
};

export default FilterTabs;

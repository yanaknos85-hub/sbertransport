import React, { FC, Suspense, useCallback } from 'react';
import { observer } from 'mobx-react';
import { Tabs } from 'antd';

import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { CargoSearch } from './Tabs/CargoSearch';
import { Tab } from '../../constants/Tabs';
import { AVAILABLE_FILTER_TABS, FilterTab, FilterTabName } from '../../constants/Filters';

import styles from './styles.module.scss';

interface IFilterProps {
  onClose: () => void;
  category: string;
}

const Filter: FC<IFilterProps> = ({ onClose, category }) => {
  const { cargoStore } = useAppStoreContext();
  const handleTabChange = useCallback((tabKey: Tab) => {
    if (AVAILABLE_FILTER_TABS.includes(tabKey)) {
      cargoStore.setActiveTabKey(tabKey);
    }
  }, []);

  return (
    <>
      <div className={styles.filterTitle}>Фильтры</div>
      <Suspense fallback={<SpinWrapped />}>
        <Tabs
          className={styles.filterTabs}
          activeKey={cargoStore.activeTabKey}
          onChange={tabKey => handleTabChange(tabKey as Tab)}
        >
          <Tabs.TabPane tab={FilterTabName.cargo} key={FilterTab.cargo}>
            <CargoSearch onClose={onClose} category={category} />
          </Tabs.TabPane>
        </Tabs>
      </Suspense>
    </>
  );
};

export default observer(Filter);

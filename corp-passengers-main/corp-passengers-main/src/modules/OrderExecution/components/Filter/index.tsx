import React, { FC, Suspense, useState } from 'react';
import { observer } from 'mobx-react';
import { Tabs } from 'antd';

import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { PassengerSearch } from './Tabs/PassengerSearch';
import { FilterTab, FilterTabName } from '../../constants/Filters';

import styles from './styles.module.scss';

interface IFilterProps {
  onClose: () => void;
}

const Filter: FC<IFilterProps> = ({ onClose }) => {
  const [activeTabKey, setActiveTabKey] = useState<FilterTab>(FilterTab.passengers);

  return (
    <>
      <div className={styles.filterTitle}>Фильтры</div>
      <Suspense fallback={<SpinWrapped />}>
        {/* TODO проверить с бизнесом - если не нужен фильтр "мои заявки", то убрать эти табы, оставить только PassengerSearch */}
        <Tabs
          className={styles.filterTabs}
          activeKey={activeTabKey}
          onChange={tabKey => setActiveTabKey(tabKey as FilterTab)}
        >
          {/* больше не имеет смысла, тк тут мы управляем только пассажирскими заявками. нужен пересмотр бзнес логики
          <Tabs.TabPane
            tab={FilterTabName.all}
            key={FilterTab.all}
            disabled
          /> */}
          <Tabs.TabPane
            tab={FilterTabName.myApplications}
            key={FilterTab.myApplications}
            disabled
          />
          <Tabs.TabPane tab={FilterTabName.passengers} key={FilterTab.passengers}>
            <PassengerSearch onClose={onClose} />
          </Tabs.TabPane>
        </Tabs>
      </Suspense>
    </>
  );
};

export default observer(Filter);

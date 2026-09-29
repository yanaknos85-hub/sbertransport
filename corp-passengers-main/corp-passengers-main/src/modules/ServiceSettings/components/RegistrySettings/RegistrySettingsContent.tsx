import React from 'react';
import { TabPane } from 'shared/components/Tab/TabPane';
import ExecutorGroupContent from 'modules/ExecutorGroup/ExecutorGroupContent';
import { Tab } from 'shared/components/Tab';
import styles from './Components/SharedSettings/Shared.module.scss';

const RegistrySettingsContent = () => {
  return (
    <Tab
      className={styles.Tabs}
      destroyInactiveTabPane
    >
      <TabPane
        tab="Группы исполнителей"
        theme="card"
        key={undefined}
      >
        <ExecutorGroupContent />
      </TabPane>
    </Tab>
  );
};

export default RegistrySettingsContent;

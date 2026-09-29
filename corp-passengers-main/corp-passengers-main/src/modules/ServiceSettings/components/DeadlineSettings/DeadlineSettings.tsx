import React, { FC } from 'react';
import { TabPane } from 'shared/components/Tab/TabPane';
import { Tab } from 'shared/components/Tab';
import DeadlineSettingsContent from './DeadlineSettingsContent';
import styles from './DeadlineSettings.module.scss';

export const DeadlineSettings: FC = () => {
  return (
    <Tab className={styles.tabContainer}>
      <TabPane tab="Пассажирские перевозки" key="Пассажирские перевозки">
        <DeadlineSettingsContent />
      </TabPane>
    </Tab>
  );
};

export default DeadlineSettings;

import React from 'react';
import { Tabs } from 'antd';
import { StepItemContentProps } from 'shared/components/Steps';
import { serviceTypes, serviceTypesTitles } from '../../constants/tripSettings';
// import SharedRideSettingsContent from './SharedRideSettingsContent';

import styles from '../../TripSettings.module.scss';

export const SharedRideSettings: React.FC<StepItemContentProps> = () => {
  return (
    <div className={styles.sharedRidesSettingsWrapper}>
      <Tabs>
        <Tabs.TabPane
          tab={serviceTypesTitles[serviceTypes.CARGO_TRANSPORTATION]}
          key={serviceTypes.CARGO_TRANSPORTATION}
        >
          {/* <SharedRideSettingsContent /> */}
          {/* Совмещение поездок?? грузоперевозки */ }
        </Tabs.TabPane>
      </Tabs>
    </div>
  );
};

export default SharedRideSettings;

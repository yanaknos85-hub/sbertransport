import React from 'react';
import { Tabs } from 'antd';
import { StepItemContentProps } from 'shared/components/Steps';
import { serviceTypes, serviceTypesTitles } from '../../constants/tripSettings';
import SharedRideSettingsContent from './SharedRideSettingsContent';

import styles from '../../TripSettings.module.scss';

export const SharedRideSettings: React.FC<StepItemContentProps> = () => {
  return (
    <div className={styles.sharedRidesSettingsWrapper}>
      <Tabs>
        <Tabs.TabPane
          tab={serviceTypesTitles[serviceTypes.EMPLOYEE_TRANSPORTATION]}
          key={serviceTypes.EMPLOYEE_TRANSPORTATION}
        >
          <SharedRideSettingsContent />
        </Tabs.TabPane>
      </Tabs>
    </div>
  );
};

export default SharedRideSettings;

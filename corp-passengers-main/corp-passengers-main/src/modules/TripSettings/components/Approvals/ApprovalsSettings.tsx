import React from 'react';
import { Tabs } from 'antd';
import { StepItemContentProps } from 'shared/components/Steps';
import { serviceTypes, serviceTypesTitles } from '../../constants/tripSettings';
import ApprovalsContent from './ApprovalsContent';

import styles from '../../TripSettings.module.scss';

export const ApprovalsSettings: React.FC<StepItemContentProps> = () => {
  return (
    <div className={styles.tripPurposesWrapper}>
      <Tabs>
        <Tabs.TabPane
          tab={serviceTypesTitles[serviceTypes.EMPLOYEE_TRANSPORTATION]}
          key={serviceTypes.EMPLOYEE_TRANSPORTATION}
        >
          <ApprovalsContent />
        </Tabs.TabPane>
      </Tabs>
    </div>
  );
};

export default ApprovalsSettings;

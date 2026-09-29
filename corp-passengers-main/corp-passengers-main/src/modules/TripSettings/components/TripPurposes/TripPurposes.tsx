import React from 'react';
import { Tabs } from 'antd';
import { StepItemContentProps } from 'shared/components/Steps';
import { serviceTypes, serviceTypesTitles } from '../../constants/tripSettings';
import TripPurposesContent from './TripPurposesContent';

import styles from '../../TripSettings.module.scss';

export const TripPurposes: React.FC<StepItemContentProps> = () => {
  return (
    <>
      <div className={styles.tripPurposesWrapper}>
        <Tabs>
          <Tabs.TabPane
            tab={serviceTypesTitles[serviceTypes.EMPLOYEE_TRANSPORTATION]}
            key={serviceTypes.EMPLOYEE_TRANSPORTATION}
          >
            <TripPurposesContent />
          </Tabs.TabPane>
        </Tabs>
      </div>
    </>
  );
};

export default TripPurposes;

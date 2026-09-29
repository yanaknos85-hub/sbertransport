import React from 'react';
import { Tabs } from 'antd';
import { StepItemContentProps } from 'shared/components/Steps';
import { serviceTypes, serviceTypesTitles } from '../../constants/tripSettings';
// import TripPurposesContent from './TripPurposesContent';

import styles from '../../TripSettings.module.scss';

export const TripPurposes: React.FC<StepItemContentProps> = () => {
  return (
    <>
      <div className={styles.tripPurposesWrapper}>
        <Tabs>
          <Tabs.TabPane
            tab={serviceTypesTitles[serviceTypes.CARGO_TRANSPORTATION]}
            key={serviceTypes.CARGO_TRANSPORTATION}
            disabled
          >
            {/* <TripPurposesContent /> */}
            {/* Цели поездок грузоперевозки */}
          </Tabs.TabPane>
        </Tabs>
      </div>
    </>
  );
};

export default TripPurposes;

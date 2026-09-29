import React, { FC } from 'react';

import ErrorBoundary from 'components/ErrorBoundary';
import { StoreNames, useAppStore } from 'ioc';

import CardsInfo from './components/CardsInfo/CardsInfo';
import Instructions from './components/Instructions/Instructions';
// import Questions from './components/Questions/Questions';
import styles from './Support.module.scss';

// Раздел Questions будет реализован в следующих релизах

const Support: FC = () => {
  const { [StoreNames.configStore]: configStore } = useAppStore();

  return (
    <ErrorBoundary>
      <div className={styles.container}>
        <CardsInfo isSDO={configStore.env.IS_SDO} />
        <Instructions />
        {/* <Questions isSDO={configStore.env.IS_SDO} /> */}
      </div>
    </ErrorBoundary>
  );
};

export default Support;

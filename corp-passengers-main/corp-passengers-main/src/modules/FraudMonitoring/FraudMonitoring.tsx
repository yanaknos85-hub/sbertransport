import React from 'react';
import type { FC } from 'react';

import FraudTable from './components/FraudTable/FraudTable';

import styles from './styles.module.scss';

const FraudMonitoring: FC = () => {
  return (
    <div className={styles.container}>
      <FraudTable />
    </div>
  );
};

export default FraudMonitoring;

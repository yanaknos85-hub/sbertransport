import React from 'react';
import { COST_DEVIATION_TITLE } from '../../constants/yandexTaxi.constants';
import { Icon } from '../Icon/Icon';

import styles from './costDeviationWarning.module.scss';

export const CostDeviationWarning = () => (
  <div className={styles.costDeviation}>
    <Icon type="warning" />
    <div className={styles.costDeviationTitle}>
      {COST_DEVIATION_TITLE}
    </div>
  </div>
);

import React from 'react';
import { WarningIcon } from './icons/WarningIcon';
import styles from './SelfPaymentYandex.module.scss';

export const SelfPaymentYandex = () => {
  return (
    <div className={styles.selfContainer}>
      <WarningIcon className={styles.selfIcon} />
      <div className={styles.selfTitleContainer}>
        <p className={styles.selfTitle}>Самостоятельная оплата</p>
        <p className={styles.selfSubtitle}>
          После подтверждения стоимости и цели руководителем, затраты будут компенсированы
        </p>
      </div>
    </div>
  );
};

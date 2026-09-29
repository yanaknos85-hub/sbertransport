import React from 'react';
import { AlertInfoIcon } from './icons/AlertInfoIcon';

import styles from './TripStatusAlert.module.scss';

export const TripStatusAlert = () => {
  return (
    <div className={styles.container}>
      <AlertInfoIcon className={styles.icon} />
      <p className={styles.title}>
        Для подтверждения стоимости необходимо добавить ссылку на чек
        из приложения Yandex GO
      </p>
    </div>
  );
};

import React, { FC } from 'react';

import { ReactComponent as Warning } from 'assets/icons/warning.svg';
import styles from './RouteNullAlert.module.scss';

/** Предупрежение о невозможности построить маршрут */
const RouteNullAlert: FC = () => (
  <div className={styles.routeNullAlert}>
    <Warning className={styles.icon} />
    <div>
      <div className={styles.title}>Невозможно построить фактический маршрут</div>
      <div>Обратите внимание на качество исполнения заявки водителем в мобильном приложении</div>
    </div>
  </div>
);

export default RouteNullAlert;

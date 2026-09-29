import React, { FC } from 'react';

import { ReactComponent as Warning } from 'assets/icons/warning.svg';
import styles from './SpoofingWarningBanner.module.scss';

const SPOOFING_BANNER_TEXT
  = 'Расчет фактического маршрута произведен с учетом "спуффинга" геокоординат с высокой погрешностью';

/** Информационная плашка о расчёте маршрута с учётом спуфинга геокоординат */
const SpoofingWarningBanner: FC = () => (
  <div className={styles.spoofingWarningBanner}>
    <Warning className={styles.icon} />
    <div>{SPOOFING_BANNER_TEXT}</div>
  </div>
);

export default SpoofingWarningBanner;

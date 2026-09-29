import React from 'react';

import Success from 'shared/components/Images/evaluationIcons/success.svg';
import styles from './failure.module.scss';

// eslint-disable-next-line react/display-name
export default (): JSX.Element => (
  <div className={styles.modalSection}>
    <div className={styles.modalFrame}>
      <div className={styles.modalContainer}>
        <div className={styles.icon}>
          <div className={styles.iconFrame}>
            <img
              alt="iconSvg"
              src={Success}
              className={styles.svg}
            />
          </div>
        </div>
        <div className={styles.textContainer}>
          <span className={styles.text}>Спасибо за обратную связь!</span>
          <span className={styles.text1}>
            <span className={styles.text2}>
              <span>
                Мы обязательно свяжемся с Вами и решим проблему. Информируем, что жалоба не является инструментом
                ускорения действий по процессу
              </span>
            </span>
          </span>
        </div>
      </div>
    </div>
  </div>
);

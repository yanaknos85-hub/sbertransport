import React from 'react';
import type { FC } from 'react';
import { Popover } from 'antd';

import { ReactComponent as QuestionIcon } from 'shared/components/Images/question.svg';
import styles from './styles.module.scss';

const AboutTariffs: FC = () => (
  <Popover
    trigger="click"
    placement="bottomRight"
    overlayClassName={styles.overlay}
    content={(
      <div className={styles.content}>
        <p>
          Тарифы на общественный транспорт зафиксированы
          в системе. Если стоимость на вашем билете выше,
          сообщите данную информацию нам через службу
          поддержки
          {' '}
          <a href="mailto:catransport@sberbank.ru">catransport@sberbank.ru</a>
        </p>
      </div>
    )}
  >
    <div className={styles.button}>
      <span>Если стоимость отличается </span>
      <QuestionIcon />
    </div>
  </Popover>
);

export default AboutTariffs;

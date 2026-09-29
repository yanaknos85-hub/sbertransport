import React from 'react';
import type { FC } from 'react';
import { Button } from 'antd';

import { useHistory } from '@sber-sbertransport/mf-core';
import { TRANSPORT_2_0_CREATE } from 'constants/constants.routes';
import folderImage from 'shared/images/emptyFolder.png';
import styles from './styles.module.scss';

const Empty: FC = () => {
  const history = useHistory();

  return (
    <div className={styles.container}>
      <img src={folderImage} alt="folder" />
      <b>Нет заявок на выбранный период</b>
      <span className={styles.container__hint}>Закажите поездку прямо сейчас</span>
      <Button
        className={styles.container__button}
        type="primary"
        onClick={() => history.push(TRANSPORT_2_0_CREATE)}
      >
        Заказать
      </Button>
    </div>
  );
};

export default Empty;

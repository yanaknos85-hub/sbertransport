import React, { useState } from 'react';
import type { FC } from 'react';
import { Modal, Checkbox, Button } from 'antd';

import { ReactComponent as CloseIcon } from './close.svg';
import styles from './styles.module.scss';

const STORAGE_KEY = 'passengersMemoAccepted';

const PassengersMemo: FC = () => {
  const [isModalVisible, setModalVisible] = useState(!localStorage.getItem(STORAGE_KEY));
  const [isButtonDisabled, setIsButtonDisabled] = useState(true);

  const onConfirmHandler = () => {
    localStorage.setItem(STORAGE_KEY, 'true');
    setModalVisible(false);
  };

  return (
    <Modal
      visible={isModalVisible}
      className={styles.passengersMemo}
      closeIcon={<CloseIcon />}
      width={640}
      footer={null}
      onCancel={() => setModalVisible(false)}
    >
      <h2 className={styles.passengersMemo__title}>Памятка о правилах пассажирских перевозок</h2>
      <p className={styles.passengersMemo__text}>
        Уважаемый пользователь!
        <br />
        <br />
        Напоминаем, что при оформлении заявки на компенсацию общественного/личного транспорта
        или заказа такси следует помнить, что поездки обязательно должны быть связаны с исполнением работником
        его служебных обязанностей и осуществляться в рабочее время согласно трудовому распорядку дня подразделения,
        за исключением доставки в ночное время и выездов на аварии.
        <br />
        <br />
        Использование транспортного обеспечения, предоставляемого Банком, в личных целях,
        во время командировок и отпуска работником
        {' '}
        <b>недопустимо</b>
        .
      </p>

      <Checkbox
        className={styles.passengersMemo__checkbox}
        onChange={e => setIsButtonDisabled(!e.target.checked)}
      >
        Я ознакомился (-ась) с предоставленной информацией
      </Checkbox>

      <Button
        className={styles.passengersMemo__button}
        type="primary"
        disabled={isButtonDisabled}
        onClick={onConfirmHandler}
      >
        Ознакомиться
      </Button>
    </Modal>
  );
};

export default PassengersMemo;

import React, { FC } from 'react';
import { Button, Modal } from 'antd';
import moment from 'moment';
import styles from './Modal.module.scss';
import { InfoContractor } from '../../types/types';
import { capitalize } from '../../utils';

interface OwnProps {
  visible: boolean;
  setVisible(visible: boolean): void;
  setMenuVisible(visible: boolean): void;
  modalData: InfoContractor;
}

export const ShowXLSModal: FC<OwnProps> = ({
  visible, setVisible, setMenuVisible, modalData,
}) => {
  const handleCancel = () => {
    setVisible(false);
    setTimeout(() => setMenuVisible(true), 300);
  };
  const currentYear = Number(moment().format('YYYY'));
  const {
    monthName, contractorId, contractorName,
  } = modalData;
  return (
    <Modal
      visible={visible}
      onCancel={handleCancel}
      footer={[
        <div className={styles.showModalButton} key={`monthd=${contractorId}`}>
          <Button className={styles.showButton} onClick={handleCancel}>
            ОК
          </Button>
        </div>,
      ]}
    >
      <p className={styles.showModalTitle}>
        {`Для выбранного контрагента реестр  "${contractorName}"/${capitalize(
          `${monthName} ${currentYear}`
        )} не был загружен!`}
      </p>
    </Modal>
  );
};

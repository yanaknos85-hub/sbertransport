import React, { useState, FC } from 'react';
import { DatePicker, Modal } from 'antd';
import Item from 'antd/lib/list/Item';
import moment from 'moment';
import type { Moment } from 'moment';

import { useDeactivateTransport } from 'api/transport/transport.api';
import { DATE_FORMAT } from 'constants/app.constants';
import { useActiveTransport } from 'modules/Vehicles/context/ActiveTransport';
import { useModalForm } from 'modules/Vehicles/context/ModalForm';
import { UUID } from 'utils/io-ts';

import styles from './TransportModalDelete.module.scss';

const TransportModalDelete: FC = () => {
  const [date, setDate] = useState<Moment>(moment());

  const { activeTransport } = useActiveTransport();
  const { stateShowModal, handleClose } = useModalForm();

  const [deactivateTransport, { isLoading }] = useDeactivateTransport(activeTransport?.id as UUID);

  const handleFinish = () => {
    if (date) {
      deactivateTransport({ exploitationEnd: date.valueOf() }).then(handleClose);
    }
  };

  if (!activeTransport) {
    return null;
  }

  const disabledDate = (date: Moment) => date < moment(activeTransport.location.exploitationStart) || date > moment();

  return (
    <Modal
      visible={stateShowModal.isOpen && stateShowModal.type === 'deleteTransport'}
      destroyOnClose
      title="Вывести из эксплуатации?"
      className={styles.modal}
      okText="Вывести"
      okButtonProps={{ disabled: !date }}
      confirmLoading={isLoading}
      onCancel={handleClose}
      onOk={handleFinish}
    >
      <div className={styles.content}>
        <span className={styles.stateNumber}>{activeTransport.stateNumber}</span>

        <p className={styles.description}>После подтверждения автомобиль будет выведен из эксплуатации</p>

        <Item title="Дата окончания эксплуатации" className={styles.date}>
          <DatePicker
            defaultValue={date}
            disabledDate={disabledDate}
            format={DATE_FORMAT.BASE_REVERTED_DOTS}
            onChange={date => setDate(date as Moment)}
          />
        </Item>
      </div>
    </Modal>
  );
};

export default TransportModalDelete;

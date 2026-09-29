import React from 'react';
import type { FC } from 'react';
import {
  Button,
  Form,
  Modal
} from 'antd';

import { useModalState } from 'shared/hooks/useModal';

import { ReactComponent as CloseIcon } from 'shared/components/Images/Close.svg';
import styles from './item.module.scss';

interface IProps {
  cancelHandler: () => void;
  cancelAndRepeatHandler: () => void;
}

export const DeclineModalEmpty: FC<IProps> = ({ cancelHandler, cancelAndRepeatHandler }) => {
  const [form] = Form.useForm();
  const [modalVisibility, modalActions] = useModalState();

  const cancelOrder = () => {
    cancelHandler();
    modalActions.hide();
    form.resetFields();
  };

  const cancelAndRepeatOrder = () => {
    cancelAndRepeatHandler();
    modalActions.hide();
    form.resetFields();
  };

  return (
    <div onClick={e => e.stopPropagation()}>
      <Button
        className={styles.button}
        onClick={modalActions.show}
        danger
        block
      >
        Отменить
      </Button>

      <Modal
        title="Отмена заявки"
        className={styles.modal}
        closeIcon={<CloseIcon />}
        open={modalVisibility}
        onCancel={modalActions.hide}
        footer={[
          <Button onClick={cancelAndRepeatOrder}>Да</Button>,
          <Button onClick={cancelOrder}>Нет</Button>,
        ]}
      >
        Необходимо создать новую заявку с параметрами отменяемой?
      </Modal>
    </div>
  );
};

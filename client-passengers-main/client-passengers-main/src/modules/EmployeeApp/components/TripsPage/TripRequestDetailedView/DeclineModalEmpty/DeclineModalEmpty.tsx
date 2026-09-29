import {
  Button, Form, Modal
} from 'antd';
import React from 'react';

import { useModalState } from 'shared/hooks/useModal';
import styles from './item.module.scss';

// the requirements https://sbtatlas.sigma.sbrf.ru/wiki/pages/viewpage.action?pageId=2020576059

export const DeclineModalEmpty = ({ cancelHandler }: { cancelHandler: (reasonValue: string) => void }): JSX.Element => {
  const [form] = Form.useForm();
  const [modalVisibility, modalActions] = useModalState();

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
        visible={modalVisibility}
        onOk={cancelHandler as any}
        onCancel={(): void => {
          modalActions.hide();
          form.resetFields();
        }}
        okText="Подтвердить отмену"
        cancelText="Вернуться"
        okButtonProps={{ danger: true }}
      />
    </div>
  );
};

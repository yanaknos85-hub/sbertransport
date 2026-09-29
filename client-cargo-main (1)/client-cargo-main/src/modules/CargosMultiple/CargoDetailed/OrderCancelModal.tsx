import React, { FC } from 'react';
import { Form, Modal, Select } from 'antd';
import TextArea from 'antd/es/input/TextArea';
import { FormInstance } from 'antd/lib/form';

import { CANCEL_ORDER_OPTIONS } from 'constants/CargoRequestStatuses.constants';

import styles from './styles.module.scss';

interface Props {
  form: FormInstance;
  reason: string;
  visible: boolean;
  textAreaVisible: boolean;
  onOk?: () => Promise<void>;
  onCancel?: () => void;
  onSelect?: (value: string, { label }: Record<string, string>) => void;
  onChange?: (value: React.ChangeEvent<HTMLTextAreaElement>) => void;
}

export const OrderCancelModal: FC<Props> = props => {
  const {
    visible,
    reason,
    onOk,
    onCancel,
    form,
    onSelect,
    onChange,
    textAreaVisible,
  } = props;

  return (
    <Modal
      destroyOnClose
      okText="Подтвердить"
      open={visible}
      title="Отменить заявку"
      okButtonProps={{ disabled: !reason }}
      onOk={onOk}
      onCancel={onCancel}
      cancelText="Отмена"
    >
      <Form form={form}>
        <Form.Item name="reason">
          <Select
            options={CANCEL_ORDER_OPTIONS}
            placeholder="Выберите причину отмены заявки"
            defaultValue={reason}
            onSelect={onSelect}
          />
          {textAreaVisible && (
            <div className={styles.textArea}>
              <Form.Item name="description">
                <TextArea
                  rows={4}
                  maxLength={250}
                  placeholder="Опишите причину отмены заявки"
                  onChange={onChange}
                />
              </Form.Item>
            </div>
          )}
        </Form.Item>
      </Form>
    </Modal>
  );
};

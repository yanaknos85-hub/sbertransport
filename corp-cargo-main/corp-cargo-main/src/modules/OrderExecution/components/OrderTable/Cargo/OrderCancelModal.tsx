import React, { FC } from 'react';
import { Form, Select } from 'antd';
import TextArea from 'antd/es/input/TextArea';
import { FormInstance } from 'antd/lib/form';

import { useTranslation } from 'i18n';
import { Modal } from 'shared/components/Modal/Modal';

import { CANCEL_ORDER_OPTIONS, CANCEL_STATUS_CODE, CancelReasons, Statuses } from '../../../constants/Cargo/Cargo';

import { useCancelOrder } from 'api/engineer';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';

import styles from './styles.module.scss';

interface Props {
  form: FormInstance;
  reason: string;
  visible: boolean;
  onCancel: () => void;
  setReason: React.Dispatch<React.SetStateAction<string>>;
  orderId: string;
  setModalVisible: React.Dispatch<React.SetStateAction<boolean>>;
  textAreaVisible: boolean
  setTextAreaVisible: React.Dispatch<React.SetStateAction<boolean>>;
}

export const OrderCancelModal: FC<Props> = (props) => {
  const {
    visible,
    reason,
    onCancel,
    form,
    textAreaVisible,
    setTextAreaVisible,
    setReason,
    orderId,
    setModalVisible
  } = props;

  const { t } = useTranslation();

  const [cancelOrder] = useCancelOrder();

  const { cargoStore } = useAppStoreContext();

  const handleChangeReason = (value: React.ChangeEvent<HTMLTextAreaElement>) => {
    setReason(value.currentTarget.value);
  };

  const handleConfirmModal = async () => {
    await cancelOrder({
      requestId: orderId,
      field: 'STATUS',
      value: Statuses.CARGO_CANCELED,
      code: CANCEL_STATUS_CODE,
      reason,
    })
    .then(() => {
      cargoStore.getCargoOrderListDeferredPost();
    })
    .catch(() => {
      form.resetFields();
    });

    setReason('');
    setModalVisible(false);
    setTextAreaVisible(false);
  };

  const handleSelectModal = (value: string, { label }: Record<string, string> ) => {
    if (value === CancelReasons.OTHER_REASON) {
      setTextAreaVisible(true);
      setReason(form.getFieldValue('description'));
    } else {
      setReason(label);
      setTextAreaVisible(false)
    }
  };

  return (
    <Modal
      destroyOnClose
      okText={t.global.confirm}
      visible={visible}
      title={t.Monitor.cancelOrderModalTitle}
      okButtonProps={{disabled: !reason}}
      onOk={handleConfirmModal}
      onCancel={onCancel}
    >
      <Form form={form}>
        <Form.Item name="reason" label="Укажите причину" labelCol={{ span: 24 }} wrapperCol={{ span: 24 }}>
          <Select
            options={CANCEL_ORDER_OPTIONS}
            placeholder={t.Monitor.clarifyReason}
            defaultValue={reason}
            onSelect={handleSelectModal}
          />
          {textAreaVisible && (
            <div className={styles.textArea}>
              <Form.Item name="description">
                <TextArea
                  rows={4}
                  maxLength={250}
                  placeholder={t.Monitor.cancelOrderDescription}
                  onChange={handleChangeReason}
                />
              </Form.Item>
            </div>
          )}
        </Form.Item>
      </Form>
    </Modal>
  )
};

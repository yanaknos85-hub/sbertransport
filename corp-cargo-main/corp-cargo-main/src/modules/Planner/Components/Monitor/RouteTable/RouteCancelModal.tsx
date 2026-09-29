import React, { FC } from 'react';
import { FormInstance } from 'antd/lib/form';
import { Form, Select } from 'antd';
import TextArea from 'antd/es/input/TextArea';

import { useTranslation } from 'i18n';

import { Modal } from 'shared/components/Modal/Modal';
import { CANCEL_ROUTE_OPTIONS, CANCEL_STATUS_CODE, CancelReasons, Statuses } from '../constants';

import { useCancelRoute } from 'api/planner';

import styles from '../styles.module.scss';

interface Props {
  form: FormInstance;
  routeId: string;
  reason: string;
  setReason: React.Dispatch<React.SetStateAction<string>>;
  visible: boolean;
  textAreaVisible: boolean;
  setTextAreaVisible: React.Dispatch<React.SetStateAction<boolean>>;
  onCancel: () => void;
  setModalVisible: React.Dispatch<React.SetStateAction<boolean>>;
}

export const RouteCancelModal: FC<Props> = (props) => {
  const { routeId, reason, setReason, visible, textAreaVisible, setTextAreaVisible, onCancel, form, setModalVisible } = props;

  const { t } = useTranslation();

  const [cancelRoute] = useCancelRoute();

  const handleConfirmModal = async () => {
    await cancelRoute({
      requestId: routeId,
      field: 'STATUS',
      value: Statuses.CARGO_CANCELED,
      code: CANCEL_STATUS_CODE,
      reason,
    })
    .catch(() => {
      form.resetFields();
    });

    setReason('');
    setTextAreaVisible(false);
    setModalVisible(false);
  };

  const handleSelect = (value, { label }: Record<string, string>) => {
    if (value === CancelReasons.OTHER_REASON) {
      setTextAreaVisible(true);
      setReason(form.getFieldValue('description'));
    } else {
      setReason(label);
      setTextAreaVisible(false)
    }
  };

  const handleChange = (value: React.ChangeEvent<HTMLTextAreaElement>) => {
    setReason(value.currentTarget.value);
  };

  return (
    <Modal
      destroyOnClose
      okText={t.global.confirm}
      visible={visible}
      title={t.Planner.cancelRouteModalTitle}
      okButtonProps={{disabled: !reason}}
      onOk={handleConfirmModal}
      onCancel={onCancel}
    >
    <Form form={form}>
      <Form.Item name="reason">
        <Select
          options={CANCEL_ROUTE_OPTIONS}
          placeholder={t.Planner.clarifyReason}
          defaultValue={reason}
          onSelect={handleSelect}
        />
        {textAreaVisible && (
          <div className={styles.textArea}>
            <Form.Item name="description">
              <TextArea
                rows={4}
                maxLength={250}
                placeholder={t.Planner.cancelRouteDescription}
                onChange={handleChange}
              />
            </Form.Item>
          </div>
        )}
        </Form.Item>
      </Form>
    </Modal>
  )
};

import {
  Button, Form, Input, Modal, Select
} from 'antd';
import React from 'react';

import { useModalState } from 'shared/hooks/useModal';
import styles from './item.module.scss';

// the requirements https://sbtatlas.sigma.sbrf.ru/wiki/pages/viewpage.action?pageId=2020576059
const commentOption = 'Другое'; // activate comment field
const DECLINE_LABELS = [
  'Ошибочный вызов автомобиля',
  'Водитель попросил сделать отмену',
  'Машины долго нет',
  'Таксист уехал в противоположном направлении',
  commentOption,
];

const declineOptions = (() => DECLINE_LABELS.map(label => ({ label, value: label })))();

const reasonOptionFieldRules = [
  {
    required: true,
    message: 'Пожалуйста, выберите причину отмены',
  },
];

const reasonFieldRules = [
  {
    required: true,
    message: 'Пожалуйста, введите причину отмены',
  },
  {
    max: 180,
    message: 'Максимальная длина 180 символов',
  },
];

interface FormFieldValues {
  reasonOption: string;
  reason: string;
  id: string;
}

export const DeclineModal = ({ cancelHandler, id }: { cancelHandler: (reasonValue: string, id: string) => void; id: string }): JSX.Element => {
  const [form] = Form.useForm();
  const [modalVisibility, modalActions] = useModalState();

  const handleFinish = (values: FormFieldValues) => {
    const reason = values.reasonOption === commentOption ? values.reason : values.reasonOption;
    cancelHandler(reason, id);
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
        visible={modalVisibility}
        onOk={form.submit}
        onCancel={(): void => {
          modalActions.hide();
          form.resetFields();
        }}
        okText="Подтвердить отмену"
        cancelText="Вернуться"
        okButtonProps={{ danger: true }}
      >
        <Form
          form={form}
          layout="vertical"
          name="decline-request"
          size="middle"
          onFinish={handleFinish}
        >
          <p>Пожалуйста, укажите причину отмены:</p>
          <Form.Item
            name="reasonOption"
            rules={reasonOptionFieldRules}
            required
          >
            <Select placeholder="Выберите причину" options={declineOptions} />
          </Form.Item>
          <Form.Item
            noStyle
            shouldUpdate={(prevValues, currentValues): boolean => prevValues.reasonOption !== currentValues.reasonOption}
          >
            {({ getFieldValue }): JSX.Element | null => getFieldValue('reasonOption') === commentOption ? (
              <Form.Item name="reason" rules={reasonFieldRules}>
                <Input.TextArea placeholder="Причина" autoSize={{ minRows: 3, maxRows: 5 }} />
              </Form.Item>
            ) : null}
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

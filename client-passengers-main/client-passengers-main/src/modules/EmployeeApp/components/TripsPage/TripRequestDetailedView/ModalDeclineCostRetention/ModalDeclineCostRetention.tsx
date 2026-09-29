import React from 'react';
import {
  Button, Modal, Form, Select, Input
} from 'antd';

import Close from 'shared/components/Images/Close.svg';
import { useModalState } from 'shared/hooks/useModal';

import './overwrite.scss';
import styles from './item.module.scss';

interface ModalDeclineCostRetentionProps {
  cancelHandler: (reasonValue: string, id: string) => void;
  id: string;
  humanReadableId: string;
  costRetention: boolean;
}

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

const ModalDeclineCostRetention: React.FC<ModalDeclineCostRetentionProps> = ({
  cancelHandler,
  id,
  humanReadableId,
  costRetention,
}) => {
  const [form] = Form.useForm();
  const [modalVisibility, modalActions] = useModalState();

  const handleFinish = (values: FormFieldValues) => {
    const reason = values.reasonOption === commentOption ? values.reason : values.reasonOption;
    cancelHandler(reason, id);
  };

  return (
    <div className="ModalDeclineCostRetention_wrapper">
      <Button
        className={styles.button}
        onClick={modalActions.show}
        danger
        block
      >
        Отменить
      </Button>
      <Modal
        open={modalVisibility}
        className="ModalDeclineCostRetention"
        onOk={form.submit}
        onCancel={(): void => {
          modalActions.hide();
          form.resetFields();
        }}
        okText="Отменить"
        cancelText="Назад"
      >
        <div className="cardWrapper">
          <div className="header">
            <div className="numberApplication">
              {`Отменить заявку ${humanReadableId} ?`}
            </div>
            <div className="cancel" onClick={() => modalActions.hide()}>
              <img alt="Close" src={Close} />
            </div>
          </div>
          { costRetention
          && <p className={styles.title}>При отмене поездки с вашего лимита будет удержана стоимость минимальной поездки</p>}
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
        </div>
      </Modal>
    </div>
  );
};

export default ModalDeclineCostRetention;

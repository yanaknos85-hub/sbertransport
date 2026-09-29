import { FC, useEffect } from 'react';
import { createPortal } from 'react-dom';
import { Form } from 'antd-mobile';
import Modal from 'components/Modal/Modal';
import Input from 'components/Input/input';
import { CheckinType } from 'constants/trips.constants';
import Hint from './Hint';
import styles from './ConfirmPositionModal.module.scss';

const hints = [
  'Нет GPS',
  'Нет связи',
];

interface ConfirmPositionModalProps {
  visible: boolean;
  onClose: () => void;
  confirm: (checkinType: CheckinType) => void;
}

const ConfirmPositionModal: FC<ConfirmPositionModalProps> = ({
  visible,
  onClose,
  confirm,
}) => {
  const [form] = Form.useForm();

  const handleConfirm = () => {
    confirm(CheckinType.MANUAL);
    onClose();
  };

  useEffect(() => {
    if (visible) {
      form.resetFields();
    }
  }, [form, visible]);

  return createPortal(
    <Modal
      visible={visible}
      onClose={onClose}
      title="Не обнаружили Вас на месте"
      actions={[
        {
          key: 'confirm',
          text: 'Подтвердить',
          primary: true,
          onClick: () => form.submit(),
        },
      ]}
      content={(
        <Form form={form} onFinish={handleConfirm}>
          <div className={styles.fieldTitle}>Укажите причину смены статуса</div>

          <Form.Item name="reason" rules={[{ required: true }]}>
            <Input placeholder="Причина" />
          </Form.Item>

          <div className={styles.hints}>
            {hints.map(hint => (
              <Hint
                key={hint}
                form={form}
                title={hint}
              />
            ))}
          </div>
        </Form>
      )}
    />,
    document.getElementById('root')!
  );
};

export default ConfirmPositionModal;

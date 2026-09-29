import React, { FC, useEffect, useState } from 'react';
import { Button, Modal, Text } from '@sber-sbertransport/ui-kit/src';
import Pincode from 'components/Pincode/Pincode';
import { Form } from 'antd';
import { useForm } from 'antd/lib/form/Form';
import { useBlockPhoneConfirmation, usePhoneConfirmation } from 'api/confirmation';
import styles from './phoneConfirmation.module.scss';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import useCount from 'shared/hooks/useCount';
import { StateConfirm } from '../../MainInformation';
import { SEND_CODE_DELAY } from '../../constants/mainInformation.constants';

interface PhoneConfirmationProps {
  visible: boolean;
  phone?: string;
  stateConfirm: StateConfirm;
  onSuccess: () => void;
  onClose: () => void;
}

const CODE_LENGTH = 4;

const initialCode = new Array(CODE_LENGTH).fill('');

const PhoneConfirmation: FC<PhoneConfirmationProps> = ({
  visible,
  onClose,
  phone,
  stateConfirm,
  onSuccess,
}) => {
  const [code, setCode] = useState(initialCode);

  const [form] = useForm();

  const [confirmPhone, { isLoading }] = usePhoneConfirmation();
  const [blockPhoneConfirmation] = useBlockPhoneConfirmation();

  const { count, setCount } = useCount();

  const onChangeCode = (values: string[]) => {
    setCode(values);
    if (values.join('').length === CODE_LENGTH && !isLoading) {
      setTimeout(form.submit);
    }
  };

  const onResetCode = () => {
    setCode(initialCode);
  };

  const close = () => {
    onResetCode();
    onClose();
  };

  useEffect(() => {
    if (stateConfirm.editPhone && (!count || count === SEND_CODE_DELAY)) {
      sendCode();
    }

    if (stateConfirm.resetCount) {
      setCount(SEND_CODE_DELAY);
    }
  }, [stateConfirm]);

  const { [StoreNames.employeeStore]: empStore } = useAppStoreContext();

  const sendCode = () => {
    if (phone) {
      empStore.editPhone(phone);
      setCount(SEND_CODE_DELAY);
    }
  };

  const onFinish = () => {
    confirmPhone({ code: code.join('') })
      .then(() => {
        onSuccess();
        close();
      })
      .catch(onResetCode);
  };

  const onAgainSendCode = () => {
    blockPhoneConfirmation()
      .then(result => {
        if (result.blocked) {
          close();
        } else {
          sendCode();
        }
      });
  };

  return (
    <Form form={form} onFinish={onFinish}>
      <Modal
        open={visible}
        onCancel={close}
        width={400}
        title="Подтвердите номер"
        footer={null}
      >
        <div className={styles.codeSendText}>Код подтверждения отправлен на номер</div>
        <Text
          strong
          size="large"
          className={styles.phone}
        >
          {phone}
        </Text>
        <div className={styles.codeText}>Код подтверждения</div>
        <Pincode
          values={code}
          onChange={(value, index, values) => onChangeCode(values)}
          type="number"
          containerClassName={styles.pinCode}
          autoTab
          size="lg"
        />
        <div className={styles.footer}>
          {count ? (
            <>
              <span>Отправить повторно через: </span>
              <span className={styles.count}>{`00:${count < 10 ? '0' : ''}${count}`}</span>
            </>
          ) : (
            <Button
              size="small"
              onClick={onAgainSendCode}
              className={styles.resend}
            >
              Отправить код повторно
            </Button>
          )}
        </div>
      </Modal>
    </Form>
  );
};

export default PhoneConfirmation;

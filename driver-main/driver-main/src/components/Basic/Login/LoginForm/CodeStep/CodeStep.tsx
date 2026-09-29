import { FC, useEffect, useState } from 'react';
import { Form } from 'antd-mobile';
import { observer } from 'mobx-react';
import { FormInstance } from 'rc-field-form/es/interface';

import Button from 'components/Button/Button';
import FormField from 'components/Form/FormField/FormField';
import { FieldType } from 'components/Form/Field/Field';
import { useAppStore } from 'stores/stores.context';

import styles from '../LoginForm.module.scss';

interface Props {
  loginForm: FormInstance;
}

const RESEND_TIME = 30;

const CodeStep: FC<Props> = observer(({ loginForm }) => {
  const { authStore: auth } = useAppStore();

  const isSubmitDisabled = !!auth.isAwaiting || !!auth.isAuthenticated;

  const onFinishHandler = (values: { code: string }): void => {
    auth.code(values.code);
  };

  const [resendTimer, setResendTimer] = useState(RESEND_TIME);

  useEffect(() => {
    const resendTimer = setTimeout(() => setResendTimer(prev => (prev ? prev - 1 : 0)), 1000);

    return () => {
      clearTimeout(resendTimer);
    };
  }, [resendTimer]);

  const resendCode = () => {
    auth.login(loginForm.getFieldValue('login'), loginForm.getFieldValue('password'));
    setResendTimer(RESEND_TIME);
  };

  return (
    <Form
      form={loginForm}
      name="login-form"
      className={styles.form}
      onFinish={onFinishHandler}
    >
      <p className={styles.headerChangePass}>
        Вам отправлено смс-сообщение с одноразовым кодом
      </p>

      <FormField
        type={FieldType.Input}
        name="code"
        params={{
          placeholder: 'Введите код из сообщения',
        }}
        rules={[{ required: true, message: 'Пожалуйста, введите код' }]}
      />
      <Button
        block
        color="primary"
        type="submit"
        className={styles.btn_submit}
        disabled={isSubmitDisabled}
        loading={auth.isAwaiting}
      >
        Войти
      </Button>
      {resendTimer ? (
        <div>
          Отправить код повторно можно через
          {' '}
          {resendTimer}
          {' '}
          секунд(ы)
        </div>
      ) : (
        <div onClick={resendCode} className={styles.resendButton}>
          Отправить код повторно
        </div>
      )}
    </Form>
  );
});

export default CodeStep;

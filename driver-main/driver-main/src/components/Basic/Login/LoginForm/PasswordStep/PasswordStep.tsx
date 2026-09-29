import { FC, useState } from 'react';
import { NavLink } from 'react-router';
import { observer } from 'mobx-react';
import { FormInstance } from 'rc-field-form/es/interface';
import { Form } from 'antd-mobile';

import { routes } from 'constants/routes.constants';
import Button from 'components/Button/Button';
import FormField from 'components/Form/FormField/FormField';
import { FieldType } from 'components/Form/Field/Field';
import { AccountLockoutModal } from 'components/LockoutComponent/AccountLockoutModal';
import { useAppStore } from 'stores/stores.context';

import styles from '../LoginForm.module.scss';

interface PasswordFormValues {
  login: string;
  password: string;
}

interface Props {
  loginForm: FormInstance;
}

const PasswordStep: FC<Props> = observer(({ loginForm }) => {
  const [isModalVisible, setModalVisible] = useState(false);
  const { authStore: auth } = useAppStore();

  const isSubmitDisabled = !!auth.isAwaiting || !!auth.isAuthenticated;

  const onFinishHandler = ({ login, password }: PasswordFormValues): void => {
    auth.login(login, password).catch(({ isLockoutAccount }) => {
      if (isLockoutAccount) {
        setModalVisible(true);
      }
    });
  };

  const handleCloseModal = (): void => {
    setModalVisible(false);
  };

  return (
    <>
      <Form
        form={loginForm}
        name="login-form"
        onFinish={onFinishHandler}
        className={styles.form}
      >
        <FormField
          type={FieldType.Input}
          name="login"
          label="Логин"
          params={{
            placeholder: 'Введите логин',
            autoComplete: 'username',
          }}
          rules={[{ required: true, message: 'Пожалуйста, введите логин' }]}
        />
        <FormField
          type={FieldType.Input}
          name="password"
          label="Пароль"
          params={{
            type: 'password',
            placeholder: 'Введите пароль',
            autoComplete: 'current-password',
          }}
          rules={[{ required: true, message: 'Пожалуйста, введите пароль' }]}
        />
        <Button
          block
          size="large"
          type="submit"
          loading={auth.isAwaiting}
          disabled={isSubmitDisabled}
          className={styles.btn_submit}
        >
          Войти
        </Button>
      </Form>
      <NavLink to={routes.ResetPassword} className={styles.link}>
        Не могу войти
      </NavLink>
      <AccountLockoutModal visible={isModalVisible} onClose={handleCloseModal} />
    </>
  );
});

export default PasswordStep;


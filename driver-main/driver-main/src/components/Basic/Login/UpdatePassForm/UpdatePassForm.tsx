import { FC } from 'react';
import { Form } from 'antd-mobile';
import { observer } from 'mobx-react';
import { FormInstance } from 'rc-field-form/es/interface';

import Button from 'components/Button/Button';
import FormField from 'components/Form/FormField/FormField';
import { FieldType } from 'components/Form/Field/Field';
import Tooltip from 'components/Tooltip/Tooltip';
import { useAppStore } from 'stores/stores.context';
import { confirmPassValidation } from 'utils';

import { ReactComponent as HelpIcon } from 'assets/icons/help.svg';
import styles from './UpdatePass.module.scss';

interface PasswordFormValues {
  password: string;
  confirm: string;
}

interface Props {
  loginForm: FormInstance;
}

const UpdatePassForm: FC<Props> = observer(({ loginForm }) => {
  const { authStore: auth } = useAppStore();

  const onFinishNewPass = (values: PasswordFormValues) => {
    const loginName = loginForm.getFieldValue('login');

    auth.updatePassword(`${loginName}:${values.password}`).then(() => {
      loginForm.resetFields(['password']);
    });
  };

  return (
    <Form
      name="new-pass-form"
      className={styles.form}
      onFinish={onFinishNewPass}
    >
      <p className={styles.headerChangePass}>
        Вы успешно вошли, используя
        <br />
        транспортный пароль, теперь
        <br />
        придумайте и запомните постоянный.
        <Tooltip
          arrow={false}
          content={(
            <p className={styles.tooltipContent}>
              <span>Пароль должен быть не менее 8 символов в длину, состоять из 6 различных символов,</span>
              <span>
                содержать только цифры, строчные и прописные буквы латинского алфавита, не должен содержать логин,
              </span>
              <span>
                более 2 одинаковых символов подряд, а также в пароле должны отсутствовать 3 рядом стоящих знака
                клавиатуры
              </span>
              <span>(как слева направо, так и справа налево), например (qwe, 456, 4rf, 741).</span>
            </p>
          )}
        >
          <HelpIcon className={styles.tooltipTrigger} />
        </Tooltip>
      </p>
      <FormField
        type={FieldType.Input}
        name="password"
        label="Новый пароль"
        params={{
          type: 'password',
          placeholder: 'Введите новый пароль',
        }}
        rules={[{ required: true, message: 'Пожалуйста, введите пароль' }]}
      />
      <FormField
        type={FieldType.Input}
        name="confirm"
        dependencies={['password']}
        label="Пароль"
        params={{
          type: 'password',
          placeholder: 'Повторите ввод пароля',
        }}
        rules={[
          {
            required: true,
            message: 'Пожалуйста, введите пароль',
          },
          confirmPassValidation,
        ]}
      />
      <Button
        block
        size="large"
        type="submit"
        className={styles.btn_submit}
        disabled={auth.isAwaiting}
        loading={auth.isAwaiting}
      >
        Сохранить
      </Button>
    </Form>
  );
});

export default UpdatePassForm;

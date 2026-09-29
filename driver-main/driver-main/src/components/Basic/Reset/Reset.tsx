import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import { Form, NavBar } from 'antd-mobile';
import { observer } from 'mobx-react';
import { RadioValue } from 'antd-mobile/es/components/radio';

import { routes } from 'constants/routes.constants';
import Button from 'components/Button/Button';
import PinCode from 'components/PinCode/PinCode';
import Radio, { RadioGroup } from 'components/Radio/Radio';
import FormField from 'components/Form/FormField/FormField';
import { FieldType } from 'components/Form/Field/Field';
import { useAppStore } from 'stores/stores.context';

import { ReactComponent as BackArrow } from 'assets/icons/back.svg';
import styles from './Reset.module.scss';

export enum ChannelType {
  EMAIL = 'EMAIL',
  SMS = 'SMS',
}

const CODE_LENGTH = 4;

const Reset = observer(() => {
  const [form] = Form.useForm();
  const [login, setLogin] = useState('');
  const [code, setCode] = useState<string>('');
  const [channel, setChannel] = useState(ChannelType.EMAIL);

  const navigate = useNavigate();

  const { authStore } = useAppStore();

  const onChangeCode = (values: string) => {
    setCode(values);
    if (values.length === CODE_LENGTH) {
      setTimeout(form.submit);
    }
  };

  const onRadioChange = (value: RadioValue) => {
    setChannel(value as ChannelType);
  };

  const onFinish = () => {
    authStore.resetPassword(login, code);
    setCode('');
  };

  const requestCode = () => {
    authStore.requestResetPasswordCode(login, channel as string);
  };

  const goBack = useCallback(() => {
    navigate(routes.Auth);
  }, []);

  useEffect(() => {
    if (authStore.passwordResetCodeAccepted) {
      goBack();
    }
  }, [authStore.passwordResetCodeAccepted, goBack]);

  return (
    <div className={styles.content}>
      <NavBar
        className={styles.nav}
        backArrow={<BackArrow />}
        onBack={goBack}
      >
        <h2 className={styles.title}>Сброс пароля</h2>
      </NavBar>
      <Form
        form={form}
        className={styles.form}
        onFinish={onFinish}
      >
        {!authStore.codeRequested ? (
          <>
            <FormField
              type={FieldType.Input}
              name="login"
              label="Введите свой логин"
              params={{
                placeholder: 'Введите логин',
                autoComplete: 'username',
                onChange: value => setLogin(value),
              }}
              rules={[{ required: true, message: 'Пожалуйста, введите логин' }]}
            />
            <h3 className={styles.subTitle}>Отправить код подтверждения по:</h3>
            <div className={styles.radioGroup}>
              <RadioGroup onChange={onRadioChange} value={channel}>
                <Radio className={styles.radio} value={ChannelType.EMAIL}>
                  Эл. почта
                </Radio>
                <Radio className={styles.radio} value={ChannelType.SMS}>
                  Телефон
                </Radio>
              </RadioGroup>
            </div>
            <Button
              block
              size="large"
              onClick={requestCode}
              className={styles.btn}
              disabled={!login || authStore.isAwaiting}
              loading={authStore.isAwaiting}
            >
              Получить код
            </Button>
          </>
        ) : (
          <>
            <div className={styles.subTitle}>Введите код подтверждения</div>
            <PinCode
              value={code}
              length={CODE_LENGTH}
              onChange={onChangeCode}
            />
            <Button
              block
              size="large"
              onClick={goBack}
              className={styles.btn}
            >
              Отмена
            </Button>
          </>
        )}
      </Form>
    </div>
  );
});

export default Reset;

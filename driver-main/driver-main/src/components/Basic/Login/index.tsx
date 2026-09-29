import { Form } from 'antd-mobile';
import { observer } from 'mobx-react';
import { useAppStore } from 'stores/stores.context';

import LoginForm from './LoginForm/LoginForm';
import UpdatePassForm from './UpdatePassForm/UpdatePassForm';

export default observer(() => {
  const { authStore: auth } = useAppStore();

  const [loginForm] = Form.useForm();

  return auth.transportPassword ? <UpdatePassForm loginForm={loginForm} /> : <LoginForm loginForm={loginForm} />;
});

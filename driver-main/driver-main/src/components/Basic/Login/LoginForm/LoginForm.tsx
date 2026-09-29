import { FormInstance } from 'rc-field-form/es/interface';
import { observer } from 'mobx-react';

import { AuthSteps } from 'constants/auth.constants';
import { useAppStore } from 'stores/stores.context';

import PasswordStep from './PasswordStep/PasswordStep';
import CodeStep from './CodeStep/CodeStep';

import styles from './LoginForm.module.scss';

const LoginForm = observer(({ loginForm }: { loginForm: FormInstance }) => {
  const {
    authStore: { step },
  } = useAppStore();

  return (
    <div className={styles.loginForm}>
      {step === AuthSteps.password && <PasswordStep loginForm={loginForm} />}
      {step === AuthSteps.code && <CodeStep loginForm={loginForm} />}
    </div>
  );
});

export default LoginForm;

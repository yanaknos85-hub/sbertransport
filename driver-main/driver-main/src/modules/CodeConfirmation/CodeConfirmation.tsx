import { FC, useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router';
import dayjs from 'dayjs';

import {
  useCheckPhoneConfirmBlock,
  useConfirmCode,
  usePhoneConfirmation,
  useProfile
} from 'api/services/DispatcherRoom/DispatcherRoom.query';
import { routes } from 'constants/routes.constants';
import { NEXT_ALLOW_CONFIRM_TIME } from 'constants/app.constants';
import NavBar from 'components/NavBar';
import PinCode from 'components/PinCode/PinCode';
import Button from 'components/Button/Button';
import useCount from 'hooks/useCount';
import { formatPhoneNumber } from 'utils/formattors/formatPhoneNumber';

import styles from './CodeConfirmation.module.scss';

export const clearPhone = (phone: string): string => phone?.replace(/[^+\d]/g, '');

const CODE_LENGTH = 4;
const SEND_CODE_DELAY = 60;

const CodeConfirmation: FC = () => {
  const navigate = useNavigate();
  const state = useLocation().state;
  const driver = useProfile().data;

  const [code, setCode] = useState<string>('');
  const [isCodeError, setCodeError] = useState(false);
  const { count, setCount } = useCount();

  const { mutateAsync: checkPhoneConfirmBlock } = useCheckPhoneConfirmBlock();
  const { mutateAsync: getConfirmCode } = useConfirmCode();
  const { mutateAsync: confirmPhone, isPending } = usePhoneConfirmation();

  const sendCode = () => {
    checkPhoneConfirmBlock().then(result => {
      if (result?.blocked) {
        navigate(-1);
      } else {
        getConfirmCode(clearPhone(driver.contactPhone));
        setCount(SEND_CODE_DELAY);
      }
    });
  };

  useEffect(() => {
    const nextAllowTime = sessionStorage.getItem(NEXT_ALLOW_CONFIRM_TIME);

    const diffInSeconds = nextAllowTime ? Math.max(0, dayjs(nextAllowTime).diff(dayjs(), 'second')) : null;

    if (diffInSeconds) {
      setCount(Number(diffInSeconds));
      return;
    }

    if (state?.checkPhoneConfirmBlock) {
      navigate(routes.CodeConfirm, { replace: true, state: undefined });
      getConfirmCode(clearPhone(driver.contactPhone));
      setCount(SEND_CODE_DELAY);
    } else {
      sendCode();
    }
  }, []);

  const onFinish = (value: string) => {
    confirmPhone(value)
      .then(() => {
        navigate(routes.SuccessPhoneConfirm);
      })
      .catch(error => {
        if (error.includes('Wrong code')) {
          setCodeError(true);
        }
      });
  };

  const onChangeCode = (value: string) => {
    setCode(value);
    setCodeError(false);
    if (value.length === CODE_LENGTH && !isPending) {
      onFinish(value);
    }
  };

  const onBack = () => {
    navigate(-1);
    sessionStorage.setItem(NEXT_ALLOW_CONFIRM_TIME, dayjs().add(count, 'second').toISOString());
  };

  return (
    <div className={styles.container}>
      <NavBar onBack={onBack}>Введите код</NavBar>
      <h3 className={styles.title}>Код подтверждения отправлен на номер</h3>
      <span className={styles.phone}>{formatPhoneNumber(driver.contactPhone)}</span>

      <div className={styles.pinCodeWrapper}>
        <span className={styles.label}>Код подтверждения</span>
        <PinCode
          value={code}
          length={CODE_LENGTH}
          className={isCodeError ? styles.codeError : undefined}
          onChange={onChangeCode}
        />
        {isCodeError && <span className={styles.error}>Неверный код</span>}
      </div>
      <div className={styles.footer}>
        {count ? (
          <>
            <span>Отправить повторно через: </span>
            <span className={styles.count}>{`00:${count < 10 ? '0' : ''}${count}`}</span>
          </>
        ) : (
          <Button
            block
            color="primary"
            fill="none"
            onClick={sendCode}
            className={styles.resend}
          >
            Отправить код повторно
          </Button>
        )}
      </div>
    </div>
  );
};

export default CodeConfirmation;

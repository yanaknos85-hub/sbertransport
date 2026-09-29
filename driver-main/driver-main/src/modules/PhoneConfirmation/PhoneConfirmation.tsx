import { FC } from 'react';
import { useNavigate } from 'react-router';

import { useCheckPhoneConfirmBlock, useProfile } from 'api/services/DispatcherRoom/DispatcherRoom.query';
import { routes } from 'constants/routes.constants';
import { NEXT_ALLOW_CONFIRM_TIME } from 'constants/app.constants';
import NavBar from 'components/NavBar';
import Button from 'components/Button/Button';
import { formatPhoneNumber } from 'utils/formattors/formatPhoneNumber';

import styles from './PhoneConfirmation.module.scss';

const PhoneConfirmation: FC = () => {
  const navigate = useNavigate();
  const driver = useProfile().data;

  const { mutateAsync: checkPhoneConfirmBlock, isPending } = useCheckPhoneConfirmBlock();

  const onConfirmationModal = () => {
    checkPhoneConfirmBlock().then(result => {
      if (result?.blocked) {
        sessionStorage.removeItem(NEXT_ALLOW_CONFIRM_TIME);
      } else if (result && 'blocked' in result && !result.blocked) {
        navigate(routes.CodeConfirm, { state: { checkPhoneConfirmBlock: true } });
      }
    });
  };

  return (
    <div className={styles.container}>
      <div className={styles.content}>
        <NavBar>Подтвердите номер</NavBar>

        <p className={styles.subTitle}>
          На указанный номер телефона придёт код подтверждения:
          <span className={styles.phone}>{formatPhoneNumber(driver.contactPhone)}</span>
        </p>
      </div>

      <Button
        block
        size="large"
        type="submit"
        loading={isPending}
        className={styles.btn}
        onClick={onConfirmationModal}
      >
        Получить код
      </Button>
    </div>
  );
};

export default PhoneConfirmation;

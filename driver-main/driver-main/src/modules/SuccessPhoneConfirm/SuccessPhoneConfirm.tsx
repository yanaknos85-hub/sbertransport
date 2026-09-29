import { FC } from 'react';
import { useNavigate } from 'react-router';

import { routes } from 'constants/routes.constants';
import Button from 'components/Button/Button';

import profile from 'assets/images/profile.png';
import { ReactComponent as SuccessIcon } from 'assets/icons/success.svg';
import styles from './SuccessPhoneConfirm.module.scss';

const SuccessPhoneConfirm: FC = () => {
  const navigate = useNavigate();

  const goToProfile = () => {
    navigate(routes.Nav, { replace: true });
    navigate(routes.Profile);
  };

  return (
    <div className={styles.container}>
      <div className={styles.content}>
        <img
          className={styles.image}
          src={profile}
          alt="Аватар"
        />
        <SuccessIcon className={styles.successIcon} />
        <h3 className={styles.title}>Номер подтвержден</h3>
        <p className={styles.subTitle}>
          Вы всегда можете изменить свой номер
          <br />
          в личном кабинете водителя
        </p>
      </div>

      <Button
        block
        size="large"
        className={styles.btn}
        onClick={goToProfile}
      >
        В личный кабинет
      </Button>
    </div>
  );
};

export default SuccessPhoneConfirm;

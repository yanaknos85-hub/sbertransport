import { FC } from 'react';
import { useNavigate } from 'react-router';

import { DriverSelf } from 'api/services/DispatcherRoom/DispatcherRoom.types';
import { routes } from 'constants/routes.constants';
import Button from 'components/Button/Button';
import { formatPhoneNumber } from 'utils/formattors/formatPhoneNumber';

import { ReactComponent as PhoneIcon } from 'assets/icons/phone.svg';
import { ReactComponent as EditIcon } from 'assets/icons/edit.svg';
import styles from './UserPhone.module.scss';

interface UserPhoneProps {
  driver: DriverSelf;
}

const UserPhone: FC<UserPhoneProps> = ({ driver }) => {
  const navigate = useNavigate();

  const onNavigate = (path: string) => () => {
    navigate(path);
  };

  return (
    <div className={styles.userPhone}>
      <div className={styles.phoneWrapper}>
        <PhoneIcon />
        <div className={styles.contactPhone}>
          <span>{formatPhoneNumber(driver.contactPhone)}</span>
          <span className={styles.phoneLabel}>Основной телефон</span>
        </div>
        <EditIcon className={styles.editIcon} onClick={onNavigate(routes.ContactPhone)} />
      </div>

      {!driver.phoneConfirmed && (
        <Button
          block
          size="large"
          className={styles.btn}
          onClick={onNavigate(routes.PhoneConfirm)}
        >
          Подтвердить
        </Button>
      )}
    </div>
  );
};

export default UserPhone;

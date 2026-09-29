import { FC } from 'react';
import { useNavigate } from 'react-router';

import { useProfile } from 'api/services/DispatcherRoom/DispatcherRoom.query';
import { routes } from 'constants/routes.constants';
import NavBar from 'components/NavBar';
import Button from 'components/Button/Button';

import UserInfo from './components/UserInfo';
import UserPhone from './components/UserPhone';
import DriverLicenseInfo from './components/DriverLicenseInfo';

import styles from './Profile.module.scss';

const Profile: FC = () => {
  const driver = useProfile().data;

  const navigate = useNavigate();

  const goAuth = () => {
    navigate(routes.Logout);
  };

  return (
    <div className={styles.profile}>
      <div className={styles.content}>
        <NavBar>Кабинет водителя</NavBar>

        <UserInfo driver={driver} />
        <span className={styles.separator} />

        <UserPhone driver={driver} />
        <span className={styles.separator} />

        <DriverLicenseInfo driver={driver} />
      </div>

      <div className={styles.footer}>
        <Button
          block
          size="large"
          color="default"
          className={styles.btnLogout}
          onClick={goAuth}
        >
          Выйти из аккаунта
        </Button>
      </div>
    </div>
  );
};

export default Profile;

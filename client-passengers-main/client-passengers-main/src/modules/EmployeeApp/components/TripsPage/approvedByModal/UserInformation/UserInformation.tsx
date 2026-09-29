import React, { useEffect, useState } from 'react';
import User from 'shared/components/Images/user.png';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import styles from './userInformation.module.scss';

interface Props {
  id: string;
  fio: string;
  position: string;
}

const UserInformation = ({
  id, fio, position,
}: Props): JSX.Element => {
  const [avatar, setAvatar] = useState<string>('');
  const {
    [StoreNames.tripStore]: tripStore,
  } = useAppStoreContext();

  useEffect(() => {
    tripStore.getUserAvatart(id)
      .then(setAvatar);
  }, []);

  return (
    <div className={styles.informationinInitiatorWrapper}>
      <div className={styles.informationinInitiatorMain}>
        <img alt="avatar" src={avatar ? avatar : User} />
        <div className={styles.informationinInitiatorInfo}>
          <span className={styles.informationinInitiatorInfoName}>
            {fio}
          </span>
          <span className={styles.informationinInitiatorPosition}>
            {position}
          </span>
        </div>
      </div>
    </div>
  );
};

export default UserInformation;

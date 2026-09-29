/* eslint-disable jsx-a11y/no-noninteractive-element-interactions */
import React, { useEffect, useState } from 'react';
import styles from '../styles.module.scss';
import User from 'shared/components/Images/user.png';
import { formatFullName } from 'utils/formatFullName';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores/StoreNames.enum';
import { parseNumber } from 'utils/parseNumber';
import { Employee } from '@sber-sbertransport/mf-core';

interface PassengersItemProps {
  item: Employee;
}

const JoinedPassengersItem: React.FC<PassengersItemProps> = ({
  item,
}) => {
  const {
    [StoreNames.tripStore]: tripStore,
  } = useAppStoreContext();

  const [avatar, setAvatar] = useState<string>('');

  useEffect(() => {
    tripStore.getUserAvatart(item.userId && item.userId)
      .then(setAvatar);
  }, []);

  const firstName = item.firstName && item.firstName;
  const lastName = item.lastName && item.lastName;
  const patronymic = item.patronymic && item.patronymic;
  const mobilePhone = item.mobilePhone && item.mobilePhone;

  return (
    <div className={styles.informationPassengersWrapper}>
      <div className={styles.informationPassengersMain}>
        <img alt="avatar" src={avatar ? avatar : User} />
        <div className={styles.informationPassengersInfo}>
          <span className={styles.informationPassengersInfoName}>
            {formatFullName(firstName, lastName, patronymic)}
          </span>
          <span className={styles.informationPassengersInfoPosition}>
            {item.positionName}
          </span>
          <span className={styles.informationPassengersInfoNumber}>
            {parseNumber(mobilePhone)}
          </span>
        </div>
      </div>
    </div>
  );
};

export default JoinedPassengersItem;

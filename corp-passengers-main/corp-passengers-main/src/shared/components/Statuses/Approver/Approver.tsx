import React from 'react';
import type { FC } from 'react';

import { useGetUserAvatar } from 'api/users';
import type { UUID } from 'utils/io-ts';
import userImage from 'shared/images/user.png';
import styles from './styles.module.scss';

interface Props {
  id: UUID;
  fullName: string;
  hasDone: boolean;
}

const Approver: FC<Props> = ({
  id, fullName, hasDone,
}) => {
  const { data: avatarImage } = useGetUserAvatar(id);

  return (
    <div className={styles.container}>
      <span className={styles.label}>{`${hasDone ? 'Заявка согласована' : 'Вашу заявку могут согласовать'}:`}</span>

      <div className={styles.approver}>
        <img src={avatarImage ?? userImage} alt="avatar" />
        <span>{fullName}</span>
      </div>
    </div>
  );
};

export default Approver;

import React, { FC } from 'react';
import { Link } from 'react-router-dom';
import { AI } from 'constants/constants.routes';
import { ROLE } from 'constants/constants.app';
import { useRole } from 'utils/useRole';
import decorLeftSrc from 'shared/images/ai-card-bg-decor1.svg';
import decorRightSrc from 'shared/images/ai-card-bg-decor2.svg';
import styles from './AICard.module.scss';

const AICard: FC = () => {
  const roles = useRole();

  if (!roles.includes(ROLE.ADMIN_DATA_MASTER)) {
    return null;
  }

  return (
    <Link to={AI} className={styles.root}>
      <span className={styles.title}>AI-помощник</span>
      <span className={styles.rightBlock}>Спросите у помощника</span>
      <img
        src={decorLeftSrc}
        alt=""
        className={styles.decorLeft}
      />
      <img
        src={decorRightSrc}
        alt=""
        className={styles.decorRight}
      />
    </Link>
  );
};

export default AICard;

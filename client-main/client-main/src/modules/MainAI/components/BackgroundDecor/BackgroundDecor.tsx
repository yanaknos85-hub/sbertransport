import React, { FC } from 'react';

import classnames from 'classnames';

import decor11 from 'shared/images/bg-decor1.1.svg';
import decor1 from 'shared/images/bg-decor1.svg';
import decor21 from 'shared/images/bg-decor2.1.svg';
import decor2 from 'shared/images/bg-decor2.svg';
import decor31 from 'shared/images/bg-decor3.1.svg';
import decor3 from 'shared/images/bg-decor3.svg';
import decor4 from 'shared/images/bg-decor4.svg';

import styles from './BackgroundDecor.module.scss';

interface BackgroundDecorProps {
  children?: React.ReactNode;
  hasMessages?: boolean;
}

const BackgroundDecor: FC<BackgroundDecorProps> = ({ children, hasMessages }) => {
  return (
    <div className={classnames(styles.container, hasMessages && styles.dimmed)}>
      <img
        className={`${styles.decor} ${styles.decorTopLeft}`}
        src={decor1}
        alt=""
      />
      <img
        className={`${styles.decor} ${styles.decorTopLeftFront}`}
        src={decor11}
        alt=""
      />
      <img
        className={`${styles.decor} ${styles.decorTopRight}`}
        src={decor2}
        alt=""
      />
      <img
        className={`${styles.decor} ${styles.decorTopRightFront}`}
        src={decor21}
        alt=""
      />
      <img
        className={`${styles.decor} ${styles.decorBottomLeft}`}
        src={decor3}
        alt=""
      />
      <img
        className={`${styles.decor} ${styles.decorBottomLeftFront}`}
        src={decor31}
        alt=""
      />
      <img
        className={`${styles.decor} ${styles.decorBottomRight}`}
        src={decor4}
        alt=""
      />
      <div className={styles.content}>
        {children}
      </div>
    </div>
  );
};

export default BackgroundDecor;

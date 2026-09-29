import React, { type FC } from 'react';
import cn from 'classnames';

import styles from './limitIndicator.module.scss';

interface LimitIndicatorProps {
  percentage: number;
}

export const LimitIndicator: FC<LimitIndicatorProps> = ({ percentage }) => {
  const getStatusClassName = () => {
    if (percentage < 31) return styles.fatal;

    if (percentage < 60) return styles.warning;

    return styles.success;
  };

  return (
    <div className={styles.container}>
      <div className={cn(styles.indicatorCircle, getStatusClassName())} />
      <span className={styles.title}>
        Лимит
        {' '}
        { percentage }
        %
      </span>
    </div>
  );
};

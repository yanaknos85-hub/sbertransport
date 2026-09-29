import React, { FC } from 'react';
import classNames from 'classnames';
import styles from './styles.module.scss';

interface TitleProps {
  title: string;
  className?: string;
}

export const TitleWithSpacer: FC<TitleProps> = ({ title, className }) => (
  <div className={styles.titleWrapper}>
    {title}
    <div className={classNames(styles.spacer, className)} />
  </div>
);

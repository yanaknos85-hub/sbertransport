import classNames from 'classnames';
import React, { FC } from 'react';

import styles from './fields.module.scss';

const Fields: FC<{ className?: string; title?: string; caption?: React.ReactNode }> = ({
  className,
  title,
  caption,
  children,
}) => (
  <div className={classNames(styles.fields, className)}>
    {title && <div className={styles.title}>{title}</div>}
    {caption && <div className={styles.caption}>{caption}</div>}
    {children}
  </div>
);

export default Fields;

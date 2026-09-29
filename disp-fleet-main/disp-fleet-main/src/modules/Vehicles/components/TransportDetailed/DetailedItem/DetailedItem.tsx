import React, { FC } from 'react';
import cn from 'classnames';

import styles from './DetailedItem.module.scss';

interface DetailedItemProps {
  title?: string;
  children: React.ReactNode;
  className?: string;
}

const DetailedItem: FC<DetailedItemProps> = ({
  title, children, className,
}) => (
  <div className={cn(styles.detailedItem, className)}>
    {title && <div className={styles.title}>{title}</div>}
    <div className={styles.body}>{children}</div>
  </div>
);

export default DetailedItem;

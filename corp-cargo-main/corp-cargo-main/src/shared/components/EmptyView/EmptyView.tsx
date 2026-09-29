import React, { FC, ReactNode } from 'react';
import Folder from 'shared/images/empty-folder.png';

import styles from './EmptyView.module.scss';

interface Props {
  title?: string;
  description?: string;
  children?: ReactNode;
}

export const EmptyView: FC<Props> = ({
  title, description, children,
}) => (
  <div className={styles.container}>
    <img
      className={styles.image}
      src={Folder}
      alt="not found"
    />
    <section className={styles.textSide}>
      <strong className={styles.title}>{title}</strong>
      <p className={styles.description}>{description}</p>
    </section>
    {children}
  </div>
);

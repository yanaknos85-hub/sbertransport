import React, { FC } from 'react';
import Folder from 'assets/images/folder.png';
import styles from './EmptyView.module.scss';

interface Props {
  title?: string;
  description?: string;
  maxWidth?: number;
}

export const EmptyView: FC<Props> = ({
  title,
  description,
  maxWidth,
}) => (
  <div className={styles.container}>
    <img
      className={styles.image}
      src={Folder}
      alt="not found"
      style={{ maxWidth }}
    />
    <section className={styles.textSide}>
      <strong className={styles.title}>{title}</strong>
      <p className={styles.description}>{description}</p>
    </section>
  </div>
);

import React, { FC } from 'react';
import { useTranslation } from 'i18n';

import styles from './styles.module.scss';

interface Title {
  name: string;
  role: string;
  date: string;
  id: string;
}

interface Props {
  title: Title;
}

export const PepBlock: FC<Props> = ({ title }) => {
  const {
    name, role, date, id,
  } = title;
  const { t: { Etrn } } = useTranslation();

  return (
    <div className={styles.wrapper}>
      <div className={styles.title}>{Etrn.card.pepBlock.title}</div>
      <div className={styles.row}>
        <div className={styles.field}>
          <span className={styles.label}>{Etrn.card.pepBlock.name}</span>
          <span className={styles.value}>{name}</span>
        </div>
        <div className={styles.field}>
          <span className={styles.label}>{Etrn.card.pepBlock.role}</span>
          <span className={styles.value}>{role}</span>
        </div>
        <div className={styles.field}>
          <span className={styles.label}>{Etrn.card.pepBlock.date}</span>
          <span className={styles.value}>{date}</span>
        </div>
        <div className={styles.field}>
          <span className={styles.label}>{Etrn.card.pepBlock.id}</span>
          <span className={styles.value}>{id}</span>
        </div>
      </div>
    </div>
  );
};

export default PepBlock;

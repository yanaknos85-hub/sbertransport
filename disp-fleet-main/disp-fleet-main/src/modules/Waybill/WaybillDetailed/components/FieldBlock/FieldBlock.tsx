import React from 'react';
import type { FC } from 'react';

import styles from './FieldBlock.module.scss';

interface Props {
  title: string;
  value: string | React.ReactNode;
}

const FieldBlock: FC<Props> = ({ title, value }) => {
  return (
    <div className={styles.fieldBlock}>
      <span className={styles.fieldTitle}>{title}</span>
      <span>{value}</span>
    </div>
  );
};

export default FieldBlock;

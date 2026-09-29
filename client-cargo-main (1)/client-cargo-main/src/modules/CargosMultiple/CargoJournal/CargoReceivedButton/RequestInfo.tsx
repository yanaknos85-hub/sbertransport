import React, { FC } from 'react';

import styles from './styles.module.scss';

interface RequestInfoProps {
  data: (string | number)[];
  labels?: string[];
}

export const RequestInfo: FC<RequestInfoProps> = ({ data, labels }) => {
  return (
    <div className={styles.requestInfo}>
      {data.map((item, index) => (
        <div key={index} className={styles.infoRow}>
          <span className={styles.label}>
            {labels?.[index]}
            :
          </span>
          <span className={styles.value}>{item}</span>
        </div>
      ))}
    </div>
  );
};

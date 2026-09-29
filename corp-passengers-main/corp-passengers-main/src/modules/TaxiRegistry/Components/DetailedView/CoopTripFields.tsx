import React, { FC } from 'react';
import uuid from 'utils/uuid';
import styles from 'shared/styles/reportsDetailedView.module.scss';
import { CoopTripFieldProps } from '../../types/types';

export const getCoopDescription = (
  description: string | string[] | number | number[] | null | undefined
): JSX.Element => {
  if (description === null || description === undefined) {
    return <span className={styles.coopTripItem}>-</span>;
  }
  if (typeof description === 'string' || typeof description === 'number') {
    return <span className={styles.coopTripItem}>{description}</span>;
  }
  return (
    Array.isArray(description)
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    && (description as any).map((item: string | number | null | undefined) => item ? (
      <span key={uuid()} className={styles.coopTripItem}>
        {item}
      </span>
    ) : (
      <span key={uuid()} className={styles.coopTripItem}>
        -
      </span>
    )
    )
  );
};

export const CoopTripField: FC<CoopTripFieldProps> = ({ description }) => (
  <div className={styles.coopTripContainer}>{getCoopDescription(description)}</div>
);

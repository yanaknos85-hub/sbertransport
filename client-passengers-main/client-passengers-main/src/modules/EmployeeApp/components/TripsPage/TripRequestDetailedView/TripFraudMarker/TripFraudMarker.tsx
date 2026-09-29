import cn from 'classnames';
import React from 'react';
import { ReactComponent as WarningIcon } from 'shared/components/Images/warning.svg';

import styles from './TripFraudMarker.module.scss';

interface TripFraudMarkerProps {
  className?: string;
  classNames?: {
    title?: string;
    description?: string;
    container?: string;
  };
  comment?: string | string[] | undefined;
  isApprover?: boolean;
}

export const TripFraudMarker = ({
  comment, className, isApprover,
}: TripFraudMarkerProps) => {
  const fraudComments = Array.isArray(comment) ? comment : [comment];

  return (
    <div className={cn(styles.fraudContainer, { [styles.withApproverFraudContainer]: isApprover }, className)}>
      <p className={cn(styles.fraudTitle, { [styles.approverTitle]: isApprover })}>
        <WarningIcon />
        Нарушение правил оформления поездки
      </p>
      {isApprover && (
        <p className={styles.fraudDescription}>
          Система антифрод обнаружила подозрительные признаки
        </p>
      )}
      {
        isApprover && comment && (
        <div className={styles.fraudAlert}>
          <ul>
            {fraudComments.map((text, index) => <li key={index}>{text}</li>)}
          </ul>
        </div>
        )
      }
    </div>
  );
};

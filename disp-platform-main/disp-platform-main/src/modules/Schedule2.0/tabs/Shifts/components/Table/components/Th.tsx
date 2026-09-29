import React, { FC } from 'react';

import { Moment } from 'moment';

import { useTableZoom } from 'modules/Schedule2.0/tabs/Shifts/context/tableZoom.context';

import styles from '../Table.module.scss';

export const Th: FC<{ time: Moment }> = ({ time }) => {
  const { cellWidth } = useTableZoom();

  return (
    <div
      className={styles.cell}
      style={{ width: cellWidth }}
    >
      <div className={styles.date}>
        {time.clone().local().format('DD.MM')}
      </div>
      <div className={styles.time}>{time.clone().local().format('HH:mm')}</div>
    </div>
  );
};

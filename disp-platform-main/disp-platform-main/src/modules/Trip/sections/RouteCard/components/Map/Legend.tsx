import React, { FC } from 'react';
import styles from './Map.module.scss';

interface LegendType {
  color: string;
  finishColor?: string;
  title: string;
}

const Legend: FC<{ legend: LegendType[]; isFinishedTrip: boolean }> = ({
  legend,
  isFinishedTrip,
}) => {
  return (
    <div className={styles.legend}>
      {legend.map(type => (
        <div key={type.title} className={styles.legendType}>
          <div
            className={styles.legendIcon}
            style={{ backgroundColor: isFinishedTrip ? type.finishColor ?? type.color : type.color }}
          />
          <div>{type.title}</div>
        </div>
      ))}
    </div>
  );
};

export default Legend;

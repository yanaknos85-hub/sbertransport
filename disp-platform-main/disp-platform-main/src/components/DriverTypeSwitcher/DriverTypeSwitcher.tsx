import React, { FC } from 'react';
import cn from 'classnames';

import styles from './driverTypeSwitcher.module.scss';

export type DriverType = 'free' | 'online' | 'all';

const driverTypes: { id: DriverType; title: string }[] = [
  { id: 'free', title: 'Только свободные' },
  { id: 'online', title: 'Текущие смены' },
  { id: 'all', title: 'Будущие смены' },
];

interface DriverTypeSwitcherProps {
  driverType: DriverType;
  setDriverType: (driverType: DriverType) => void;
}

const DriverTypeSwitcher: FC<DriverTypeSwitcherProps> = ({
  driverType,
  setDriverType,
}) => (
  <div className={styles.driverTypeSwitcher}>
    {driverTypes.map(type => (
      <div
        key={type.id}
        onClick={() => setDriverType(type.id)}
        className={cn(styles.type, { [styles.selected]: type.id === driverType })}
      >
        {type.title}
      </div>
    ))}
  </div>
);

export default DriverTypeSwitcher;

import React, { FC, useMemo } from 'react';

import cn from 'classnames';

import { rainBowIcons, RainBowType, rainRange } from './RainBow.constants';

import styles from './RainBow.module.scss';

interface Props {
  value?: number;
  className?: string;
  size?: 'small' | 'middle';
}

export const RainBow: FC<Props> = ({
  value, className, size = 'small',
}) => {
  const iconType = useMemo(
    () => {
      if (value !== undefined) {
        return (
          Object.entries(rainRange).find(([_, [min, max]]) => value >= +min && value <= +max)?.[0]
          ?? RainBowType.RAIN_BOW_FULL
        );
      }

      return RainBowType.RAIN_BOW_FULL;
    },
    [value]
  );

  return (
    <div
      className={cn(styles.rainBow, className, {
        [styles.rainBowSmall]: size === 'small',
        [styles.rainBowMiddle]: size === 'middle',
      })}
    >
      {value !== undefined && (
        <span className={cn(styles.workload)}>
          {value}
          &#37;
        </span>
      )}
      {rainBowIcons[iconType]}
    </div>
  );
};

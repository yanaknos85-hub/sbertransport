import React, { FC, useMemo } from 'react';

import { Moment } from 'moment';

import { DATE_FORMAT } from 'constants/app.constants';

import { CELL_PADDING, ONE_HOUR } from 'modules/Schedule2.0/constants/schedule.constants';
import { useTableZoom } from 'modules/Schedule2.0/tabs/Shifts/context/tableZoom.context';

import { TABLE_CELL_Y, TABLE_HEADER_Y } from '../../Table.constants';
import useCurrentTime from './useCurrentTime';

import styles from './CurrentTime.module.scss';

interface CurrentTimeProps {
  times: Moment[];
  vehicleCount: number;
  fixedColumnsWidth: number;
}

export const CurrentTime: FC<CurrentTimeProps> = ({
  times,
  vehicleCount,
  fixedColumnsWidth,
}) => {
  const currentTime = useCurrentTime();

  const { cellWidth } = useTableZoom();

  const currentTimeOffset = currentTime.diff(times[0]) / ONE_HOUR * (cellWidth + CELL_PADDING);

  const currentTimeHeight = useMemo(() => {
    const padding = 2;

    return vehicleCount ? (vehicleCount * TABLE_CELL_Y) + TABLE_HEADER_Y - padding : undefined;
  }, [vehicleCount]);

  const currentTimeVisible = times.some(time => time.clone().local().format('YYYY-MM-DD') === currentTime.format('YYYY-MM-DD'));

  if (!currentTimeVisible) return null;

  return (
    <div
      className={styles.currentTime}
      style={{
        left: `${currentTimeOffset}px`,
        marginLeft: fixedColumnsWidth,
        height: currentTimeHeight,
      }}
      title={currentTime.format(DATE_FORMAT.DATE_WITH_TIME)}
    />
  );
};

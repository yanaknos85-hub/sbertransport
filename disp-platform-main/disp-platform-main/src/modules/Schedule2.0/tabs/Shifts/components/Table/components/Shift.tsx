import React, { FC, MouseEvent, RefObject } from 'react';

import cn from 'classnames';
import { Moment } from 'moment';

import { ScheduleVehicle } from 'api/schedule2.0/schedule.types';

import { formatFullName } from 'utils/formatFullName';

import { useModals } from 'modules/Schedule2.0/context/modal.context';
import { useSelectedShift } from 'modules/Schedule2.0/context/selectedShift.context';
import { CalendarShift } from 'modules/Schedule2.0/Schedule.types';

import { useEventSize } from '../hooks/useTripSizes';
import { Name } from './Name';

import { ReactComponent as Close } from 'assets/icons/close-bold.svg';

import styles from '../Table.module.scss';

interface ShiftProps {
  shift: CalendarShift;
  vehicle: ScheduleVehicle;
  time: Moment;
  isVehicleBooked: boolean;
  table: RefObject<HTMLDivElement>;
  fixedColumnsWidth: number;
}

export const Shift: FC<ShiftProps> = ({
  shift,
  vehicle,
  time,
  isVehicleBooked,
  table,
  fixedColumnsWidth,
}) => {
  const getShiftSize = useEventSize();

  const { tripWidth, tripOffset } = getShiftSize(
    shift.calendarStartTime,
    shift.calendarEndTime,
    time
  );

  const { setSelectedShift } = useSelectedShift();

  const { handleOpenCreate, handleOpenDelete } = useModals();

  const handleClick = (onClick: () => void) => (e: MouseEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    setSelectedShift({
      ...shift,
      vehicle,
    });
    onClick();
  };

  const { firstName, lastName } = shift.driver;
  const hasEwbId = !!shift.ewbId;

  return (
    <div
      className={cn(styles.badge, styles.clickable, {
        [styles.active]: shift.active,
        [styles.booked]: isVehicleBooked,
        [styles.startDateVisible]: !shift.isStartOuter,
      })}
      style={{
        width: `${tripWidth}px`,
        left: `${tripOffset}px`,
      }}
      role="button"
      onClick={handleClick(handleOpenCreate)}
      tabIndex={-1}
      key={shift.id}
    >
      <div className={styles.close} onClick={handleClick(handleOpenDelete)}>
        <Close />
      </div>
      <Name
        name={formatFullName(firstName, lastName, '')}
        table={table}
        fixedColumnsWidth={fixedColumnsWidth}
        hasEwbId={hasEwbId}
      />
    </div>
  );
};

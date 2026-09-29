import React, { FC, MouseEvent } from 'react';

import cn from 'classnames';
import moment, { Moment } from 'moment';

import { ScheduleVehicle } from 'api/schedule2.0/schedule.types';

import { useModals } from 'modules/Schedule2.0/context/modal.context';
import { CalendarTrip } from 'modules/Schedule2.0/Schedule.types';
import { useSelectedTrip } from 'modules/Schedule2.0/tabs/Shifts/context/selectedTrip.context';

import { useEventSize } from '../hooks/useTripSizes';

import styles from '../Table.module.scss';

interface TripProps {
  trip: CalendarTrip;
  vehicle: ScheduleVehicle;
  time: Moment;
}

export const Trip: FC<TripProps> = ({
  trip,
  vehicle,
  time,
}) => {
  const getTripSizes = useEventSize();

  const { tripWidth, tripOffset } = getTripSizes(
    trip.calendarStartTime,
    trip.calendarEndTime,
    time
  );

  const { setSelectedTrip } = useSelectedTrip();
  const { handleOpenOrder } = useModals();

  const handleClick = (e: MouseEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    setSelectedTrip({
      ...trip,
      vehicle,
    });
    handleOpenOrder();
  };

  const startTime = trip.driverProcessingTime ?? trip.factStartTime ?? trip.expectedStartTime;
  const endTime = trip.factEndTime ?? trip.expectedEndTime;
  const title = `${trip.humanReadableId}, ${moment(startTime).format('DD.MM HH:mm')} - ${moment(endTime).format('DD.MM HH:mm')}`;

  return (
    <div
      className={cn(styles.badge, styles.trip, {
        [styles.clickable]: trip.ordered,
        [styles.planning]: trip.planning,
        [styles.active]: moment().isBetween(moment(startTime), moment(endTime)),
        [styles.factTime]: trip.factStartTime || trip.factEndTime,
        [styles.booked]: trip.ordered,
        [styles.startDateVisible]: !trip.isStartOuter,
      })}
      style={{
        width: `${tripWidth}px`,
        left: `${tripOffset}px`,
      }}
      title={title}
      onClick={trip.ordered ? handleClick : undefined}
      key={trip.id}
    >
      <span className={styles.tripId}>{trip.humanReadableId}</span>
    </div>
  );
};

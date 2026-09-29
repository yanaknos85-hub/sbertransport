import { useEffect, useMemo } from 'react';

import moment, { Moment } from 'moment';

import { useAPIQueryCache } from 'api';
import { useProfile } from 'api/profile/profile.api';
import { ScheduleKeys, useCargoVehicleBusyness, usePassVehicleBusyness } from 'api/schedule2.0/schedule.api';
import { Busyness } from 'api/schedule2.0/schedule.types';

import { TRIP_STATUSES } from 'constants/trips.constants';

import { useTripsContext } from 'context/Trips.context';

import { UUID } from 'utils/io-ts';

import { getCalendarEndTime, getCalendarStartHour, getCalendarStartTime } from 'modules/Schedule2.0/Schedule.lib';
import { CalendarTrip } from 'modules/Schedule2.0/Schedule.types';
import { useShiftsQuery } from 'modules/Schedule2.0/tabs/Shifts/context/shiftsQuery.context';

const addCalendarDataToBusyness = (busyness: Busyness[], times: Moment[]) => busyness.map(({ trips, ...vehicle }) => ({
  ...vehicle,
  trips: trips.map((trip): CalendarTrip => {
    const startTime = trip.driverProcessingTime ?? trip.factStartTime ?? trip.expectedStartTime;
    const calendarStartHour = getCalendarStartHour(startTime, times);
    const calendarStartTime = getCalendarStartTime(startTime, times);
    const calendarEndTime = getCalendarEndTime(trip.factEndTime ?? trip.expectedEndTime, times);

    return {
      ...trip,
      calendarStartTime,
      calendarStartHour,
      calendarEndTime,
      isStartOuter: moment(calendarStartTime).isAfter(moment.utc(startTime)),
    };
  }),
}));

export const useBusynessData = (vehicleIds: UUID[], times: Moment[]) => {
  const { contractorId } = useProfile().data;
  const { query } = useShiftsQuery();

  const passBusynessData = usePassVehicleBusyness({
    startTime: query.startDate,
    endTime: query.endDate,
    contractorId,
    vehicleIds,
  }, {
    suspense: false,
    refetchOnWindowFocus: false,
  }).data;

  const passBusyness = useMemo(() => {
    return addCalendarDataToBusyness(passBusynessData ?? [], times);
  }, [passBusynessData, times]);

  const cargoBusynessData = useCargoVehicleBusyness({
    startTime: query.startDate,
    endTime: query.endDate,
    contractorId,
    vehicleIds,
  }, {
    suspense: false,
    refetchOnWindowFocus: false,
  }).data;

  const cargoBusyness = useMemo(() => {
    return addCalendarDataToBusyness(cargoBusynessData ?? [], times);
  }, [cargoBusynessData, times]);

  const { lastMessage } = useTripsContext();
  const cache = useAPIQueryCache();

  useEffect(() => {
    // При получении новой поездки, назначении водителя, отмене или заверешении поездки обновляем табл
    if (
      lastMessage?.data.isNew
      || lastMessage?.data.status === TRIP_STATUSES.DRIVER_ASSIGNED
      || lastMessage?.data.status === TRIP_STATUSES.ORDER_CANCELLED_BY_CLIENT
      || lastMessage?.data.status === TRIP_STATUSES.ORDER_CANCELLED_BY_DRIVER
      || lastMessage?.data.status === TRIP_STATUSES.ORDER_FINISHED
    ) {
      cache.refetchQueries([ScheduleKeys.PassBusyness]);
    }
  }, [lastMessage, cache]);

  return {
    passBusyness,
    cargoBusyness,
  };
};

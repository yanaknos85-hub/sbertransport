import { useMemo } from 'react';
import { useCheckinInfo } from 'api/services/Trips/Trips.query';
import { PassTrip } from 'api/services/Trips/Trips.types';
import { TRIP_STATUSES } from 'constants/trips.constants';

/** Хук для определения статуса поездки по чекинам. Помогает определить, на какой точке сейчас находится водитель */
export const useNextWaypoint = (trip: PassTrip) => {
  const checkinInfo = useCheckinInfo({ tripId: trip.id }).data;

  const statusesOrder = useMemo(() => {
    // Для каждого промежуточно статуса добавляет по 2 статуса
    const intermediateWaypoinStatuses = new Array(trip.waypoints.slice(1, -1).length * 2)
      .fill(null)
      .map((_, index) => index % 2 === 0
        ? TRIP_STATUSES.INTERMEDIATE_WAYPOINT_ARRIVED
        : TRIP_STATUSES.TRIP_IN_PROGRESS
      );

    return [
      TRIP_STATUSES.DRIVER_ARRIVED,
      TRIP_STATUSES.TRIP_IN_PROGRESS,
      ...intermediateWaypoinStatuses,
      TRIP_STATUSES.ORDER_FINISHED,
    ];
  }, [trip]);

  const statusInfo = useMemo(() => {
    if (!checkinInfo) return {
      nextStatusIndex: null,
      nextWaypointIndex: null,
    };

    const intermediateWaypointCheckins = checkinInfo.chekins.filter(
      checkin => checkin.status === TRIP_STATUSES.INTERMEDIATE_WAYPOINT_ARRIVED
    ).length;

    const driverArrivedCheckins = checkinInfo.chekins.filter(
      checkin => checkin.status === TRIP_STATUSES.DRIVER_ARRIVED
    ).length;

    const lastPointIndex = intermediateWaypointCheckins + driverArrivedCheckins;
    const maxStatusIndex = lastPointIndex * 2; // потому что по 2 статуса на каждую точку
    const currentStatus = checkinInfo.chekins.at(-1)?.status;
    const isProgressStatus = currentStatus === TRIP_STATUSES.TRIP_IN_PROGRESS
      || currentStatus === TRIP_STATUSES.DRIVER_ON_THE_WAY;
    const delta = isProgressStatus ? 0 : 1;

    return {
      nextStatusIndex: maxStatusIndex - delta,
      nextWaypointIndex: lastPointIndex - delta,
    };
  }, [checkinInfo]);

  return {
    nextStatus: statusInfo.nextStatusIndex !== null ? statusesOrder[statusInfo.nextStatusIndex] : null,
    nextWaypointIndex: statusInfo.nextWaypointIndex,
  };
};

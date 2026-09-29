import { useMemo } from 'react';
import { WaypointType, WaypointWithType } from 'types/waypoint';
import { CheckinType, TRIP_STATUSES } from 'constants/trips.constants';
import { CheckinInfo, PassTrip } from 'api/trips/trips.types';

interface CheckTypeProps {
  index: number;
  length: number;
  status: TRIP_STATUSES;
  checkinInfo?: CheckinInfo;
}

interface CheckType {
  type: WaypointType;
  checkinType?: CheckinType;
  arriveTime?: string;
  leaveTime?: string;
}

const checkType = ({
  index,
  length,
  status,
  checkinInfo,
}: CheckTypeProps): CheckType => {
  if (status === TRIP_STATUSES.ORDER_FINISHED && !checkinInfo?.chekins.length) {
    return { type: 'PASSED_NO_CHECKIN' };
  }

  switch (index) {
    case 0: {
      const arrivedCheckin = checkinInfo?.chekins.find(x => x.status === TRIP_STATUSES.DRIVER_ARRIVED);

      if (checkinInfo?.chekins.some(x => x.status === TRIP_STATUSES.TRIP_IN_PROGRESS)) {
        return {
          type: `PASSED_${arrivedCheckin?.type ?? 'MANUAL'}`,
          checkinType: arrivedCheckin?.type,
          arriveTime: arrivedCheckin?.time,
          leaveTime: checkinInfo?.chekins.find(x => x.status == TRIP_STATUSES.TRIP_IN_PROGRESS)?.time,
        };
      }

      if (arrivedCheckin) {
        return {
          type: `IN_PROGRESS_${arrivedCheckin.type}`,
          arriveTime: arrivedCheckin?.time,
        };
      }

      return { type: 'ON_THE_WAY' };
    }

    case length - 1: {
      const orderFinishedCheckin = checkinInfo?.chekins.find(x => x.status === TRIP_STATUSES.ORDER_FINISHED);

      if (orderFinishedCheckin) {
        return {
          type: `PASSED_${orderFinishedCheckin.type}`,
          checkinType: orderFinishedCheckin.type,
          arriveTime: orderFinishedCheckin.time,
        };
      }

      if (checkinInfo?.chekins.filter(x => x.status === TRIP_STATUSES.TRIP_IN_PROGRESS).length === index) {
        return { type: 'ON_THE_WAY' };
      }

      return { type: 'EXPECTED' };
    }

    default: {
      const tripInProgressCheckins = checkinInfo?.chekins.filter(
        x => x.status === TRIP_STATUSES.TRIP_IN_PROGRESS
      ) ?? [];

      if (tripInProgressCheckins.length > index) {
        const currentCheckin = checkinInfo?.chekins.filter(
          x => x.status === TRIP_STATUSES.INTERMEDIATE_WAYPOINT_ARRIVED
        )?.[index - 1];

        return {
          type: `PASSED_${currentCheckin?.type ?? 'MANUAL'}`,
          checkinType: currentCheckin?.type,
          arriveTime: currentCheckin?.time,
          leaveTime: tripInProgressCheckins[index]?.time,
        };
      }

      if (tripInProgressCheckins.length === index) {
        const currentCheckin = checkinInfo?.chekins.filter(
          x => x.status === TRIP_STATUSES.INTERMEDIATE_WAYPOINT_ARRIVED
        )?.[index - 1];

        if (currentCheckin) {
          return {
            type: `IN_PROGRESS_${currentCheckin.type}`,
            arriveTime: currentCheckin.time,
          };
        }

        return { type: 'ON_THE_WAY' };
      }

      return { type: 'EXPECTED' };
    }
  }
};

/**
 * @returns
 * - waypoints - массив waypoints с доп полем type
 */
export const useWaypoints = (trip: PassTrip, checkinInfo?: CheckinInfo) => {
  const waypointsWithType: WaypointWithType[] = useMemo(() => trip.waypoints.map((waypoint, index) => {
    const {
      type, checkinType, arriveTime, leaveTime,
    } = checkType({
      index,
      length: trip.waypoints.length,
      status: trip.status,
      checkinInfo,
    });

    return {
      ...waypoint,
      type,
      checkinType,
      arriveTime,
      leaveTime,
    };
  }), [trip, checkinInfo]);

  return { waypoints: waypointsWithType };
};

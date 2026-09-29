import { WaypointType } from 'types/waypoint';

export const getWaypointActionText = (
  type: WaypointType,
  index: number,
  passengerCount?: number
): string => {
  switch (type) {
    case 'PASSED_AUTO':
    case 'PASSED_MANUAL':
    case 'PASSED_NO_CHECKIN':
    case 'IN_PROGRESS_AUTO':
    case 'IN_PROGRESS_MANUAL': {
      if (index === 0 || passengerCount) {
        return 'ПОСАДКА';
      }

      return 'ВЫСАДКА';
    }

    case 'ON_THE_WAY': {
      return 'В ПУТИ';
    }

    default: {
      return '';
    }
  }
};

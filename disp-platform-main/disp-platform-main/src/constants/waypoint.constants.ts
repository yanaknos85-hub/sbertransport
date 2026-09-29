import { WaypointType } from 'types/waypoint';

export const colorMap: Record<WaypointType, string> = {
  PASSED_AUTO: 'rgb(25, 177, 80)',
  PASSED_MANUAL: 'rgb(255, 154, 50)',
  PASSED_NO_CHECKIN: 'rgb(255, 87, 67)',
  IN_PROGRESS_AUTO: 'rgb(105, 121, 247)',
  IN_PROGRESS_MANUAL: 'rgb(105, 121, 247)',
  ON_THE_WAY: 'rgb(105, 121, 247)',
  EXPECTED: 'rgb(204, 204, 204)',
};

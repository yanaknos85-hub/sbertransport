import { letterEndingMinutes } from 'shared/hooks/letterEndingFactory';
import { TWaypoint, WaypointField } from 'shared/models/geo/types';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';

import { buildAddressString, waypointIsValid } from 'utils/waypoint/core';

/**
 * Метод получает строку о времени ожидания
 * в нужном формате
 */
export const getWaitingTimeString = ({
  waitingTime,
  index,
  waypoints,
}: {
  waitingTime: number;
  index: number;
  waypoints: TWaypoint[];
}): string => {
  const isTransit = index > 0 && index < waypoints.length - 1;
  return isTransit && waitingTime > 0 ? ` | ${waitingTime} ${letterEndingMinutes(waitingTime)} ожидания` : '';
};

/**
 * Метод получает поля waypoint'ов
 * для работы с формой
 * @param waypoints
 */
export const getWaypointFields = (waypoints: WaypointModel[]): WaypointField[] => waypoints.map((waypoint, index) => ({
  ...waypoint,
  fieldName: `WayPointField--${waypoint.latitude}-${waypoint.longitude}-${index}`,
  addressString: buildAddressString(waypoint),
  waitingTimeString: getWaitingTimeString({
    waitingTime: waypoint.waitTimeMinutes,
    index,
    waypoints,
  }),
  isValid: waypointIsValid(waypoint),
}));

export const getInitialValues = (
  waypointFields: WaypointField[]
): Record<string, string | undefined> => {
  const result: Record<string, string | undefined> = {};
  waypointFields.forEach(waypointField => {
    result[waypointField.fieldName] = waypointField.absenceReason;
  });
  return result;
};

import { Waypoint } from 'api/trips/trips.types';
import { CheckinType } from 'constants/trips.constants';

/**
  Возможные варианты цветов и текстов:
  1. Точка уже пройдена (Посадка/Высадка):
    - зеленый, если авто                              PASSED_AUTO
    - желтый, если ручной                             PASSED_MANUAL
    - красный, если статус завершен, а чекина нет     PASSED_NO_CHECKIN
  2. Водитель на точке (Посадка/Высадка)
    - фиолетовый                                      IN_PROGRESS_AUTO
    - фиолетовый                                      IN_PROGRESS_MANUAL
  3. Водитель в пути к точке (в пути)                 ON_THE_WAY
    - фиолетовый
  4. Водитель еще не в пути (нет статуса)             EXPECTED
    - серый
*/
export type WaypointType =
  | 'PASSED_AUTO'
  | 'PASSED_MANUAL'
  | 'PASSED_NO_CHECKIN'
  | 'IN_PROGRESS_AUTO'
  | 'IN_PROGRESS_MANUAL'
  | 'ON_THE_WAY'
  | 'EXPECTED';

export type WaypointWithType = Waypoint & {
  type: WaypointType;
  checkinType?: CheckinType;
  arriveTime?: string;
  leaveTime?: string;
};

import { WaypointType } from 'types/waypoint';

export const waypointTypeTitle: Record<WaypointType, string> = {
  PASSED_AUTO: 'Автоматическая отметка на точке',
  PASSED_MANUAL: 'Ручная отметка на точке без подтверждения геопозиции',
  PASSED_NO_CHECKIN: 'Геопозиция не отмечалась',
  IN_PROGRESS_AUTO: 'Автоматическая отметка на точке',
  IN_PROGRESS_MANUAL: 'Ручная отметка на точке без подтверждения геопозиции',
  ON_THE_WAY: '',
  EXPECTED: '',
};

export const bgColorMap: Record<WaypointType, string> = {
  PASSED_AUTO: 'rgba(16, 191, 106, 0.15)',
  PASSED_MANUAL: 'rgba(255, 154, 50, 0.15)',
  PASSED_NO_CHECKIN: 'rgba(254, 91, 59, 0.15)',
  IN_PROGRESS_AUTO: 'rgba(105, 121, 247, 0.15)',
  IN_PROGRESS_MANUAL: 'rgba(105, 121, 247, 0.15)',
  ON_THE_WAY: 'rgba(105, 121, 247, 0.15)',
  EXPECTED: '',
};

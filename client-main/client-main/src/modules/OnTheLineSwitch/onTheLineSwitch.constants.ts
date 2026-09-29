export enum TelemechanicOrderStatus {
  IN_PROGRESS = 'IN_PROGRESS',
  DONE = 'DONE',
  WARNING = 'WARNING',
  DECLINED = 'DECLINED',
  ON_THE_LINE = 'ON_THE_LINE',
  IN_GARAGE = 'IN_GARAGE',
  EXPIRED = 'EXPIRED',
  CANCELED = 'CANCELED',
  FINISHED = 'FINISHED',
  REVISION = 'REVISION',
}

export const onTheLineSubtitle: Record<TelemechanicOrderStatus, string> = {
  [TelemechanicOrderStatus.IN_PROGRESS]: 'Осмотр',
  [TelemechanicOrderStatus.DONE]: 'На проверке',
  [TelemechanicOrderStatus.WARNING]: 'На проверке',
  [TelemechanicOrderStatus.ON_THE_LINE]: 'Активен',
  [TelemechanicOrderStatus.IN_GARAGE]: 'Неактивен',
  [TelemechanicOrderStatus.EXPIRED]: 'Неактивен',
  [TelemechanicOrderStatus.CANCELED]: 'Неактивен',
  [TelemechanicOrderStatus.FINISHED]: 'Неактивен',
  [TelemechanicOrderStatus.REVISION]: 'Неактивен',
  [TelemechanicOrderStatus.DECLINED]: 'Не пройдено',
};

export enum telemechanicRoutes {
  FLEET_TELEMECHANIC = 'fleet/telemechanic',
  FLEET_TELEMECHANIC_EWB = 'fleet/telemechanic/ewb',
  FLEET_TELEMECHANIC_EWB_FAILED = 'fleet/telemechanic/ewbfailed',
}

export const otlStorageKey = 'otlStatus';

export const titles = {
  onTheLine: 'На линии',
  inactive: 'Неактивен',
};

export const WAYBILL = `/telemechanic/ewb`;
export const WAYBILL_SEARCH = `${WAYBILL}/search`;
export const WAYBILL_UUID = `${WAYBILL}/uuid`;
export const WAYBILL_ONE = `${WAYBILL}/:ewbId`;
export const WAYBILL_CLOSE = `${WAYBILL}/close`;
export const WAYBILL_CANCEL = `${WAYBILL_ONE}/cancel`;
export const WAYBILL_FIRST_TITLE = `${WAYBILL}/form-title/1`;
export const WAYBILL_TITLE_SEND = `${WAYBILL}/title/send`;

export enum InspectionType {
  MEDIC = 'MEDIC',
  TECHNIC = 'TECHNIC',
  TELEMEDIC = 'TELEMEDIC',
}

export enum InspectionTypeName {
  MEDIC = 'Медицинский осмотр',
  TECHNIC = 'Технический осмотр',
  TELEMEDIC = 'Телемедицина',
}

export enum WaybillStatus {
  EWB_CREATED = 'EWB_CREATED',
  MEDIC_IN_PROGRESS = 'MEDIC_IN_PROGRESS',
  MEDIC_DECLINED = 'MEDIC_DECLINED',
  TELEMECH_IN_PROGRESS = 'TELEMECH_IN_PROGRESS',
  DRIVER_CANCELED = 'DRIVER_CANCELED',
  TELEMECH_DECLINED = 'TELEMECH_DECLINED',
  ON_THE_LINE = 'ON_THE_LINE',
  KORUS_DECLINED = 'KORUS_DECLINED',
  IN_GARAGE = 'IN_GARAGE',
  EWB_CLOSED = 'EWB_CLOSED',
  EXPIRED = 'EXPIRED',
  EWB_CANCELLED = 'EWB_CANCELLED',
}

export const WaybillStatusesNames: Record<WaybillStatus, string> = {
  [WaybillStatus.EWB_CREATED]: 'ЭПЛ создан',
  [WaybillStatus.MEDIC_IN_PROGRESS]: 'Прохождение медика',
  [WaybillStatus.MEDIC_DECLINED]: 'Медик не пройден',
  [WaybillStatus.TELEMECH_IN_PROGRESS]: 'Прохождение телемеханика',
  [WaybillStatus.DRIVER_CANCELED]: 'Отменена водителем',
  [WaybillStatus.TELEMECH_DECLINED]: 'Телемеханик не пройден',
  [WaybillStatus.ON_THE_LINE]: 'На линии',
  [WaybillStatus.KORUS_DECLINED]: 'Отказ в формировании QR',
  [WaybillStatus.IN_GARAGE]: 'Заезд в парк',
  [WaybillStatus.EWB_CLOSED]: 'ЭПЛ закрыт',
  [WaybillStatus.EXPIRED]: 'ЭПЛ просрочен',
  [WaybillStatus.EWB_CANCELLED]: 'ЭПЛ отменен',
} as const;

export const DeclinedWaybillStatuses: readonly WaybillStatus[] = [
  WaybillStatus.MEDIC_DECLINED,
  WaybillStatus.DRIVER_CANCELED,
  WaybillStatus.TELEMECH_DECLINED,
  WaybillStatus.KORUS_DECLINED,
] as const;

export const CancelWaybillStatuses: readonly WaybillStatus[] = [
  WaybillStatus.EWB_CREATED,
  WaybillStatus.MEDIC_IN_PROGRESS,
  WaybillStatus.TELEMECH_IN_PROGRESS,
] as const;

export enum Passing {
  Success = 'Пройден',
  Unsuccess = 'Не пройден',
}

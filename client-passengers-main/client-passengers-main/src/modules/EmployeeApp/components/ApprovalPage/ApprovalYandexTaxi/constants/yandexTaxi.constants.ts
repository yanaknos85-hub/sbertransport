import { YandexTaxiRequestStatus } from 'api/yandexTaxi/yandex-taxi.constants';

export const activeStatuses = [
  YandexTaxiRequestStatus.CONFIRMATION,
];
export const closedStatuses = [
  YandexTaxiRequestStatus.PAYMENT_AWAITING,
  YandexTaxiRequestStatus.PAYMENT_DONE,
  YandexTaxiRequestStatus.PAYMENT_NOT_DONE,
  YandexTaxiRequestStatus.ORDER_PAYMENT_FORMATION,
  YandexTaxiRequestStatus.CONFIRMED,
  YandexTaxiRequestStatus.DECLINED,
];

/** порог отклонения стоимости поездки от плановой, для вывода предупреждения */
export const COST_DEVIATION_TRESHOLD = 0.1;
export const COST_DEVIATION_TITLE = `Отклонение от плановой стоимости более ${COST_DEVIATION_TRESHOLD * 100}%`;

export const YANDEX_LIMITS_PROGRESS_STROKE_COLOR = 'rgb(25, 177, 80)';
export const YANDEX_LIMITS_PROGRESS_FILL_COLOR = 'rgb(224, 224, 224)';

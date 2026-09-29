import { YandexTaxiRequestStatus } from 'api/yandexTaxi/yandex-taxi.constants';

export const TRIPS_YANDEX_FINISHED_STATUSES = [YandexTaxiRequestStatus.CANCELLED, YandexTaxiRequestStatus.DECLINED, YandexTaxiRequestStatus.PAYMENT_DONE, YandexTaxiRequestStatus.PAYMENT_NOT_DONE];
export const TRIPS_YANDEX_ACTIVE_STATUSES = [YandexTaxiRequestStatus.ORDER_PAYMENT_FORMATION, YandexTaxiRequestStatus.CONFIRMATION, YandexTaxiRequestStatus.CONFIRMATION_NEEDED, YandexTaxiRequestStatus.CONFIRMED, YandexTaxiRequestStatus.NEW];

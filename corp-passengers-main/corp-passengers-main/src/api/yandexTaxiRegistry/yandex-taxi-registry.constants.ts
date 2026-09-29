export const GET_YANDEX_TAXI_REGISTRY_LIST = `/request/external/report`;
export const GET_YANDEX_TAXI_REGISTRY_ITEM = `/request/external/:id`;
export const GET_YANDEX_TAXI_REGISTRY_ITEM_RECEIPT = `/request/external/:id/files`;
export const GET_YANDEX_TAXI_REGISTRY_REPORT = `/request/external/files/registry`;
export const GET_YANDEX_TAXI_REGISTRY_PAYMENT = '/request/external/files/payment-report-registry';

// ECONOMY Эконом
// COMFORT Комфорт
// BUSINESS Улучшенный комфорт (в терминологии яндекса Комфорт+)
// VIP Максимальный класс (в терминологии яндекса Бизнес)
// используем только эконом и комфорт

export enum YandexTaxiTariff {
  ECONOMY = 'ECONOMY',
  COMFORT = 'COMFORT',
}

export const YandexTaxiTariffTitles: Record<YandexTaxiTariff, string> = {
  [YandexTaxiTariff.ECONOMY]: 'Эконом',
  [YandexTaxiTariff.COMFORT]: 'Комфорт',
};

export enum YandexTaxiRequestStatus {
  NEW = 'NEW',
  CONFIRMATION_NEEDED = 'CONFIRMATION_NEEDED',
  CONFIRMATION = 'CONFIRMATION',
  DATA_NEEDED = 'DATA_NEEDED',
  CONFIRMED = 'CONFIRMED',
  DECLINED = 'DECLINED',
  CANCELLED = 'CANCELLED',
  ORDER_PAYMENT_FORMATION = 'ORDER_PAYMENT_FORMATION',
  PAYMENT_AWAITING = 'PAYMENT_AWAITING',
  PAYMENT_DONE = 'PAYMENT_DONE',
  PAYMENT_NOT_DONE = 'PAYMENT_NOT_DONE',
  GENAI_CHECK = 'GENAI_CHECK',
}

export const YandexTaxiRequestStatusTitles: Record<YandexTaxiRequestStatus, string> = {
  [YandexTaxiRequestStatus.NEW]: 'Новая заявка',
  [YandexTaxiRequestStatus.CONFIRMATION_NEEDED]: 'Требуется подтверждение завершения поездки',
  [YandexTaxiRequestStatus.CONFIRMATION]: 'Ждет согласования',
  [YandexTaxiRequestStatus.DATA_NEEDED]: 'Требуются дополнительные сведения',
  [YandexTaxiRequestStatus.CONFIRMED]: 'Подтверждено',
  [YandexTaxiRequestStatus.DECLINED]: 'Отклонено',
  [YandexTaxiRequestStatus.CANCELLED]: 'Заявка отменена',
  [YandexTaxiRequestStatus.ORDER_PAYMENT_FORMATION]: 'Формирование приказа на выплату',
  [YandexTaxiRequestStatus.PAYMENT_DONE]: 'Выплата произведена',
  [YandexTaxiRequestStatus.PAYMENT_NOT_DONE]: 'Выплата не произведена',
  [YandexTaxiRequestStatus.PAYMENT_AWAITING]: 'Ожидание выплаты',
  [YandexTaxiRequestStatus.GENAI_CHECK]: 'Проверка Gen-AI',
};

export enum ResponseFormat {
  LIST = 'LIST',
  FULL = 'FULL',
  REGISTRY = 'REGISTRY',
}

export enum SortFields {
  HUMAN_READABLE_ID = 'humanReadableId',
  DESIRED_DATE = 'tripDate',
  EXPECTED_COST = 'plannedCost',
  STATUS = 'status',
  TARIFF = 'tariff',
}

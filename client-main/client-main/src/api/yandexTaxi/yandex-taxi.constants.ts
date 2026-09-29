export const GET_YANDEX_TAXI_ORDERS_LIST = `/request/external/`;

export enum YandexTaxiTariff {
  ECONOMY = 'ECONOMY',
  COMFORT = 'COMFORT',
}

export enum YandexTaxiRequestStatus {
  NEW = 'NEW',
  CONFIRMATION_NEEDED = 'CONFIRMATION_NEEDED',
  CONFIRMATION = 'CONFIRMATION',
  DATA_NEEDED = 'DATA_NEEDED',
  CONFIRMED = 'CONFIRMED',
  DECLINED = 'DECLINED',
  CANCELLED = 'CANCELLED',
}

export enum ResponseFormat {
  LIST = 'LIST',
  FULL = 'FULL',
  REGISTRY = 'REGISTRY',
}

export enum RequestType {
  PASSENGER = 'PASSENGER',
  APPROVAL = 'APPROVAL',
}

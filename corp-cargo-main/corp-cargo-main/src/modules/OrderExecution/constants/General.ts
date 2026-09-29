import type { SearchResponse } from 'modules/OrderExecution/interfaces/Orders.types';

export const FORMAT = 'DD.MM.YYYY, HH:mm';

export const VALUE_NOT_FOUND = '-';
export const DEADLINE_IS_VIOLATED = 'Нарушен';
export const DEADLINE_IS_NOT_VIOLATED = ' Не нарушен';

export enum TransportType {
  OFFICIAL = 'OFFICIAL',
  PRIVATE = 'PRIVATE',
  SPECIAL = 'SPECIAL',
}

export const CarTypeName: Record<TransportType, string> = {
  [TransportType.OFFICIAL]: 'Служебный',
  [TransportType.PRIVATE]: 'Личный',
  [TransportType.SPECIAL]: 'Специальный',
};

export enum latinCharsToCyrillicList {
  latin = 'ABEKMHOPCTYXS',
  rus = 'АВЕКМНОРСТУХС',
}

// Пустой объект для инициализации cargoOrderList
export const EMPTY_SEARCH_RESPONSE: SearchResponse = {
  totalPages: 0,
  totalElements: 0,
  size: 10,
  content: [],
  number: 0,
  sort: { empty: true, sorted: false, unsorted: true },
  numberOfElements: 0,
  pageable: { offset: 0, sort: { empty: true, sorted: false, unsorted: true }, pageNumber: 0, pageSize: 10, paged: false, unpaged: true },
  first: true,
  last: true,
  empty: true,
};

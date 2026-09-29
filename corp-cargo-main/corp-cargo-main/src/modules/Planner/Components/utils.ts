import {
  AddressType,
  OrderWaypointMinimalType,
  OrderWaypointType,
  RouteAddressType,
  RouteWaypointType,
  OrderType,
  OrderListMinimalType,
} from '../types';

export const renderPointType = (type: 'LOAD' | 'UNLOAD' | 'LOAD_UNLOAD') => {
  switch (type) {
    case 'LOAD':
      return 'Сбор';
    case 'UNLOAD':
      return 'Доставка';
    case 'LOAD_UNLOAD':
      return 'Сбор/Доставка';
    default:
      break;
  }
};

export const getAddressString = (address: RouteAddressType | AddressType): string =>
  `${address.city}, ${address.street}, ${address.house}`;

export const isTypeLoadLast = (index: number, listLength: number, type: string): boolean =>
  index === listLength - 1 && type === 'LOAD';

export const isTypeUnloadFirst = (index: number, type: string): boolean =>
  index === 0 && type === 'UNLOAD';

export const sortOrders = (a: RouteWaypointType, b: RouteWaypointType): number => {
  if (a.orderingIndex > b.orderingIndex) return 1;
  if (a.orderingIndex < b.orderingIndex) return -1;
  return 0;
};

export const declension = (number: number): string => {
  const words = ['адрес', 'адреса', 'адресов'];
  return words[
    number % 100 > 4 && number % 100 < 20 ? 2 : [2, 0, 1, 1, 1, 2][number % 10 < 5 ? Math.abs(number) % 10 : 5]
    ];
};

export const getRouteCoordinates = (waypoints: RouteWaypointType[]) =>
  waypoints?.map(point => ({
    longitude: point.address.longitude,
    latitude: point.address.latitude,
  })) || [];

export const getOrderCoordinates = (waypoints: OrderWaypointMinimalType[]) =>
  waypoints?.map(point => ({
    longitude: point.longitude,
    latitude: point.latitude,
  })) || [];

import { TransportTypes } from 'stores/TransportTypes/TransportTypes.interface';

// для каких сервисов/типов транспорта разрешены совместные поездки, пока нет бэка
export const allowedTransportTypesForSharedRides = [
  TransportTypes.TAXI,
  TransportTypes.PERSONAL,
  TransportTypes.CARSHARING,
  TransportTypes.DEDICATED,
  TransportTypes.DOMESTIC_COURIER,
  TransportTypes.INTERREGIONAL,
];

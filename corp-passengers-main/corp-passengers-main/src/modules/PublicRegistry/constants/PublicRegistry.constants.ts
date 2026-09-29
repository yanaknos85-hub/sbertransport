import moment from 'moment';

export const travellingBehavior = [
  { label: 'Да', value: '1' },
  { label: 'Нет', value: '2' },
];

export enum SortFields {
  PASSENGER_FULL_NAME = 'PASSENGER_FULL_NAME',
  CREATION_DATE = 'CREATION_DATE',
  EXPECTED_COST = 'EXPECTED_COST',
  REQUEST_HUMAN_ID = 'REQUEST_HUMAN_ID',
}

export const UserSortProperties = {
  SortProperty: 'sortProperty',
  DirectionAsc: 'directionAsc',
};

export const coopTripLabel = 'Совместная';
export const individualTripLabel = 'Индивидуальная';

export const initialFormValues = {
  orderPaymentFormationStartDate: { mode: 'range', value: [moment().startOf('month'), moment()] },
};

const cityTransports = [
  {
    label: 'Автобус',
    value: 'CITY_BUS',
  },
  {
    label: 'Троллейбус',
    value: 'CITY_TROLLEYBUS',
  },
  {
    label: 'Трамвай',
    value: 'CITY_TRAM',
  },
  {
    label: 'Метро',
    value: 'CITY_METRO',
  },
];

const suburbTransports = [
  {
    label: 'Междугородный автобус',
    value: 'SUBURB_BUS',
  },
  {
    label: 'Пригородный поезд',
    value: 'SUBURB_TRAIN',
  },
  {
    label: 'Междугородный троллейбус',
    value: 'SUBURB_TROLLEYBUS',
  },
];

const travelTransports = [
  {
    label: 'Автобус',
    value: 'TRAVEL_CARD_BUS',
  },
  {
    label: 'Троллейбус',
    value: 'TRAVEL_CARD_TROLLEYBUS',
  },
  {
    label: 'Трамвай',
    value: 'TRAVEL_CARD_TRAM',
  },
  {
    label: 'Метро',
    value: 'TRAVEL_CARD_METRO',
  },
  {
    label: 'Единый',
    value: 'TRAVEL_CARD_ALL_CITY_TRANSPORT',
  },
];

const paidTransports = [
  {
    label: 'Платная парковка',
    value: 'PAID_PARKING',
  },
  {
    label: 'Платная дорога',
    value: 'TOLL_ROAD',
  },
  {
    label: 'Переправа',
    value: 'PAID_FERRY_CROSSING',
  },
];

export const TypePublicTransportOptions = {
  CITY_TRIP_COMPENSATION: cityTransports,
  SUBURB_TRIP_COMPENSATION: suburbTransports,
  TRAVEL_CARD_COMPENSATION: travelTransports,
  PAID_SERVICES_COMPENSATION: paidTransports,
};

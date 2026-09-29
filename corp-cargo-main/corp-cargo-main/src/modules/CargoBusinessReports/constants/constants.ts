import moment from 'moment';
import { FilterRequest } from '../types/types';

export enum SortFields {
  FULL_NAME = 'FULL_NAME',
  CREATION_DATE = 'CREATION_DATE',
  EXPECTED_COST = 'EXPECTED_COST',
  REQUEST_HUMAN_ID = 'REQUEST_HUMAN_ID',
}

export const SortProperties = {
  SortProperty: 'sortProperty',
  DirectionAsc: 'directionAsc',
};

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
    label: 'Паромная переправа',
    value: 'SUBURB_FERRY_CROSSING',
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

export const Statuses = {
  WAIT: 'В очереди',
  IN_PROGRESS: 'В процессе',
  DONE: 'Завершена',
  CANCELED: 'Отменена',
  ERROR: 'Ошибка',
} as const;

export const FIELD_LABELS: Record<keyof FilterRequest, string> = {
  requestHumanId: 'Номер заявки',
  cargoTransportType: 'Вид транспорта',
  contractorSet: 'Контрагент',
  authorFIO: 'ФИО создателя',
  requestStatusSet: 'Статус поездки',
  deadlineDate: 'Контрольный срок',
  desiredDate: 'Дата отправления',
  creationDate: 'Дата создания заявки',
  changeDate: 'Дата изменения заявки',
  organizationSet: 'Организация',
  department1: 'Подразделение 1 уровня',
  department2: 'Подразделение 2 уровня',
  department3: 'Подразделение 3 уровня',
  department4: 'Подразделение 4 уровня',
  department5: 'Подразделение 5 уровня',
  department6: 'Подразделение 6 уровня',
  status: 'Статус',
  sortSetting: 'Настройки сортировки',
  organizationId: 'ID организации',
  empty: '',
};

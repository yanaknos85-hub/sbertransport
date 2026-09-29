
import { MOCKED_API_PREFIX } from 'constants/constants.env';
import { GeneralFilterFieldsDefault } from './index';

export enum Statuses {
  RECORD_FIELD_SERVICE = 'RECORD_FIELD_SERVICE',
  PERFORMER_ON_WAY = 'PERFORMER_ON_WAY',
  DEFINING_WORKS = 'DEFINING_WORKS',
  APPROVE_WORKS_COST = 'APPROVE_WORKS_COST',
  CARRY_OUT = 'CARRY_OUT',
  SEARCH_TRUCK = 'SEARCH_TRUCK',
  TRUCK_ON_WAY = 'TRUCK_ON_WAY',
  CAR_PASSED_TRUCK = 'CAR_PASSED_TRUCK',
  ACCEPTING = 'ACCEPTING',
  ISSUING_A_CAR = 'ISSUING_A_CAR',
  RECORD_FOR_TIRE = 'RECORD_FOR_TIRE',
  WAITING_FOR_TIRE = 'WAITING_FOR_TIRE',
  FINISHED = 'FINISHED',
  CANCELED = 'CANCELED',
}

export enum TireTypes {
  WHEEL_REPAIR = 'WHEEL_REPAIR',
  SEASON_REPAIR = 'SEASON_REPAIR',
}

export enum Objectives {
  ALL = 'ALL',
  TIRE = 'TIRE',
  DISK = 'DISK',
  COMPLETE_WHEEL = 'COMPLETE_WHEEL',
  TIRE_SEPARATELY = 'TIRE_SEPARATELY',
}

export const StatusNames: Record<Statuses, string> = {
  [Statuses.RECORD_FIELD_SERVICE]: 'Запись на Выездной сервис',
  [Statuses.PERFORMER_ON_WAY]: 'Исполнитель в пути',
  [Statuses.DEFINING_WORKS]: 'Определение перечня работ',
  [Statuses.APPROVE_WORKS_COST]: 'Согласование стоимости работ',
  [Statuses.CARRY_OUT]: 'Проведение работ',
  [Statuses.SEARCH_TRUCK]: 'Поиск эвакуатора',
  [Statuses.TRUCK_ON_WAY]: 'Эвакуатор едет к вам',
  [Statuses.CAR_PASSED_TRUCK]: 'Машина передана на эвакуатор',
  [Statuses.ACCEPTING]: 'Машина принята на шиномонтаж',
  [Statuses.ISSUING_A_CAR]: 'Выдача автомобиля',
  [Statuses.RECORD_FOR_TIRE]: 'Запись на шиномонтаж',
  [Statuses.WAITING_FOR_TIRE]: 'Вас ожидают на шиномонтаже',
  [Statuses.FINISHED]: 'Завершено',
  [Statuses.CANCELED]: 'Отменено',
};

export const DefaultStatuses = [
  Statuses.RECORD_FIELD_SERVICE,
  Statuses.PERFORMER_ON_WAY,
  Statuses.DEFINING_WORKS,
  Statuses.APPROVE_WORKS_COST,
  Statuses.CARRY_OUT,
  Statuses.SEARCH_TRUCK,
  Statuses.TRUCK_ON_WAY,
  Statuses.CAR_PASSED_TRUCK,
  Statuses.ACCEPTING,
  Statuses.ISSUING_A_CAR,
  Statuses.RECORD_FOR_TIRE,
  Statuses.WAITING_FOR_TIRE,
];

export const TireTypeNames: Record<TireTypes, string> = {
  [TireTypes.WHEEL_REPAIR]: 'Ремонт колеса',
  [TireTypes.SEASON_REPAIR]: 'Сезонный шиномонтаж',
};

export const ObjectivesNames: Record<Objectives, string> = {
  [Objectives.ALL]: 'Все',
  [Objectives.TIRE]: 'Шина',
  [Objectives.DISK]: 'Диск',
  [Objectives.COMPLETE_WHEEL]: 'Колеса в сборе',
  [Objectives.TIRE_SEPARATELY]: 'Шины отдельно',
};

export const FilterFieldsDefault = {
  requestStatusSet: DefaultStatuses,
  ...GeneralFilterFieldsDefault,
};

export const TIRE = 'tire';

export const ORDER_DETAILED = `/еngineers/monitor/${TIRE}`;

export const MONITORING = `${MOCKED_API_PREFIX}/${TIRE}/monitoring`;
export const MONITORING_ORDER = `${MONITORING}/:orderId`;
export const TAKE_TO_WORK = `${MONITORING_ORDER}/take_to_work`;

import { MOCKED_API_PREFIX } from 'constants/constants.env';
import { GeneralFilterFieldsDefault } from './index';

export enum Statuses {
  SEARCH_TRUCK = 'SEARCH_TRUCK',
  TRUCK_ON_WAY = 'TRUCK_ON_WAY',
  CAR_PASSED_TRUCK = 'CAR_PASSED_TRUCK',
  RECORD_FOR_MAINTENANCE = 'RECORD_FOR_MAINTENANCE',
  WAITING_FOR_MAINTENANCE = 'WAITING_FOR_MAINTENANCE',
  ACCEPTING = 'ACCEPTING',
  DIAGNOSTICS = 'DIAGNOSTICS',
  APPROVE_WORKS_COST = 'APPROVE_WORKS_COST',
  CARRY_OUT = 'CARRY_OUT',
  ISSUING_A_CAR = 'ISSUING_A_CAR',
  FINISHED = 'FINISHED',
  CANCELED = 'CANCELED',
}

export const StatusNames: Record<Statuses, string> = {
  [Statuses.SEARCH_TRUCK]: 'Поиск эвакуатора',
  [Statuses.TRUCK_ON_WAY]: 'Эвакуатор едет к вам',
  [Statuses.CAR_PASSED_TRUCK]: 'Машина передана на эвакуатор',
  [Statuses.RECORD_FOR_MAINTENANCE]: 'Запись на СТО',
  [Statuses.WAITING_FOR_MAINTENANCE]: 'Вас ожидают на СТО',
  [Statuses.ACCEPTING]: 'Машина принята на СТО',
  [Statuses.DIAGNOSTICS]: 'Диагностика автомобиля',
  [Statuses.APPROVE_WORKS_COST]: 'Согласование Заказ-наряда',
  [Statuses.CARRY_OUT]: 'Проведение работ',
  [Statuses.ISSUING_A_CAR]: 'Выдача автомобиля',
  [Statuses.FINISHED]: 'Завершено',
  [Statuses.CANCELED]: 'Отменено',
};

export const DefaultStatuses = [
  Statuses.SEARCH_TRUCK,
  Statuses.TRUCK_ON_WAY,
  Statuses.CAR_PASSED_TRUCK,
  Statuses.RECORD_FOR_MAINTENANCE,
  Statuses.WAITING_FOR_MAINTENANCE,
  Statuses.ACCEPTING,
  Statuses.DIAGNOSTICS,
  Statuses.APPROVE_WORKS_COST,
  Statuses.CARRY_OUT,
  Statuses.ISSUING_A_CAR,
];

export const FilterFieldsDefault = {
  requestStatusSet: DefaultStatuses,
  ...GeneralFilterFieldsDefault,
};

export const MAINTENANCE = 'maintenance';

export const ORDER_DETAILED = `/engineers/monitor/${MAINTENANCE}`;

export const MONITORING = `${MOCKED_API_PREFIX}/${MAINTENANCE}/monitoring`;
export const ORDER = `${MONITORING}/:orderId`;
export const TAKE_TO_WORK = `${ORDER}/take_to_work`;
export const DELETE_WORK_ORDER = `${ORDER}/work_order`;
export const SAVE_WORK_ORDER_PRICE = `${MONITORING}/revision`;

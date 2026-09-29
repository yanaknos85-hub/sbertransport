import { MOCKED_API_PREFIX } from 'constants/constants.env';
import { GeneralFilterFieldsDefault } from './index';

export enum Statuses {
  SEARCH_FOR_A_TOW_TRUCK = 'SEARCH_FOR_A_TOW_TRUCK',
  TOW_TRUCK_IS_COMING = 'TOW_TRUCK_IS_COMING',
  TOW_TRUCK_FILED = 'TOW_TRUCK_FILED',
  CAR_IS_ON_THE_WAY = 'CAR_IS_ON_THE_WAY',
  CAR_DELIVERED_TO_DESTINATION = 'CAR_DELIVERED_TO_DESTINATION',
  FINISHED = 'FINISHED',
  CANCELED = 'CANCELED',
}

export enum Types {
  STANDARD = 'STANDARD',
  MANIPULATOR = 'MANIPULATOR',
  TRACTOR = 'TRACTOR',
}

export const StatusNames: Record<Statuses, string> = {
  [Statuses.SEARCH_FOR_A_TOW_TRUCK]: 'Поиск эвакуатора',
  [Statuses.TOW_TRUCK_IS_COMING]: 'Эвакуатор едет к вам',
  [Statuses.TOW_TRUCK_FILED]: 'Эвакуатор подан',
  [Statuses.CAR_IS_ON_THE_WAY]: 'Машина в пути',
  [Statuses.CAR_DELIVERED_TO_DESTINATION]: 'Машина доставлена в пункт назначения',
  [Statuses.FINISHED]: 'Завершено',
  [Statuses.CANCELED]: 'Отменено',
};

export const DefaultStatuses = [
  Statuses.SEARCH_FOR_A_TOW_TRUCK,
  Statuses.TOW_TRUCK_IS_COMING,
  Statuses.TOW_TRUCK_FILED,
  Statuses.CAR_IS_ON_THE_WAY,
  Statuses.CAR_DELIVERED_TO_DESTINATION,
];

export const TypeNames: Record<Types, string> = {
  [Types.STANDARD]: 'Стандарт',
  [Types.MANIPULATOR]: 'Манипулятор',
  [Types.TRACTOR]: 'Тягач',
};

export const FilterFieldsDefault = {
  requestStatusSet: DefaultStatuses,
  ...GeneralFilterFieldsDefault,
};

export const EVACUATION = 'evacuation';

export const ORDER_DETAILED = `/engineers/monitor/${EVACUATION}`;

export const MONITORING = `${MOCKED_API_PREFIX}/${EVACUATION}/monitoring`;
export const MONITORING_ORDER = `${MONITORING}/:orderId`;
export const TAKE_TO_WORK = `${MONITORING_ORDER}/take_to_work`;

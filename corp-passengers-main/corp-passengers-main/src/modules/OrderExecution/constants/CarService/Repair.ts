import { MOCKED_API_PREFIX } from 'constants/constants.env';
import { GeneralFilterFieldsDefault } from './index';

export enum Statuses {
  REPAIR_SEARCH_FOR_A_TOW_TRUCK = 'SEARCH_FOR_A_TOW_TRUCK',
  REPAIR_TOW_TRUCK_IS_COMING = 'TOW_TRUCK_IS_COMING',
  REPAIR_CAR_WAS_TRANSFERRED_TO_A_TOW_TRUCK = 'CAR_WAS_TRANSFERRED_TO_A_TOW_TRUCK',
  REPAIR_REGISTRATION_AT_THE_SERVICE_STATION = 'REGISTRATION_AT_THE_SERVICE_STATION',
  REPAIR_REGISTRATION_FOR_FIELD_SERVICE = 'REGISTRATION_FOR_FIELD_SERVICE',
  REPAIR_PERFORMER_ON_THE_WAY = 'PERFORMER_ON_THE_WAY',
  REPAIR_DETERMINATION_OF_THE_LIST_OF_WORKS = 'DETERMINATION_OF_THE_LIST_OF_WORKS',
  REPAIR_EXPECTED_AT_THE_SERVICE_STATION = 'EXPECTED_AT_THE_SERVICE_STATION',
  REPAIR_CAR_WAS_ACCEPTED_AT_THE_SERVICE_STATION = 'CAR_WAS_ACCEPTED_AT_THE_SERVICE_STATION',
  REPAIR_DIAGNOSTICS = 'DIAGNOSTICS',
  REPAIR_WORK_ORDER_APPROVAL = 'WORK_ORDER_APPROVAL',
  REPAIR_WORK = 'WORK',
  REPAIR_ISSUING_A_CAR = 'ISSUING_A_CAR',
  REPAIR_FINISHED = 'FINISHED',
  REPAIR_CANCELED = 'CANCELED',
}

export enum Problems {
  TRAFFIC_ACCIDENT = 'TRAFFIC_ACCIDENT',
  MAINTENANCE = 'MAINTENANCE',
  TOW_TRUCK = 'TOW_TRUCK',
  DOORS_AND_WINDOWS = 'DOORS_AND_WINDOWS',
  DIAGNOSTICS = 'DIAGNOSTICS',
  ENGINE = 'ENGINE',
  TRANSMISSION = 'TRANSMISSION',
  CLUTCH = 'CLUTCH',
  WHEEL_REPAIR = 'WHEEL_REPAIR',
  BRAKES = 'BRAKES',
  CHASSIS = 'CHASSIS',
  COOLING = 'COOLING',
  STEERING = 'STEERING',
  ELECTRICAL_EQUIPMENT = 'ELECTRICAL_EQUIPMENT',
  CAR_INTERIOR = 'CAR_INTERIOR',
  OTHER = 'OTHER',
}

export enum Defects {
  DOOR_LOCK = 'DOOR_LOCK',
  DOOR_CLOSING_SENSOR = 'DOOR_CLOSING_SENSOR',
  DOOR_HINGES = 'DOOR_HINGES',
  POWER_WINDOW = 'POWER_WINDOW',
  CAR_NOT_OPEN = 'CAR_NOT_OPEN',
  CHECK_ENGINE_IS_ON = 'CHECK_ENGINE_IS_ON',
  OIL_LEAK = 'OIL_LEAK',
  BELT_BROKE = 'BELT_BROKE',
  EXTRANEOUS_SOUND = 'EXTRANEOUS_SOUND',
  ENGINE_OVERHEATING = 'ENGINE_OVERHEATING',
  TRANSMISSION_DOES_NOT_TURN_ON = 'TRANSMISSION_DOES_NOT_TURN_ON',
  KNOCKS_OUT_THE_TRANSMISSION = 'KNOCKS_OUT_THE_TRANSMISSION',
  CANT_TURN_ON_ALL_WHEEL_DRIVE = 'CANT_TURN_ON_ALL_WHEEL_DRIVE',
  GRINDING_OF_PADS = 'GRINDING_OF_PADS',
  BRAKE_FLUID_LEAK = 'BRAKE_FLUID_LEAK',
  HAND_BRAKE = 'HAND_BRAKE',
  SUSPENSION_FRONT = 'SUSPENSION_FRONT',
  SUSPENSION_BACK = 'SUSPENSION_BACK',
  RADIATOR = 'RADIATOR',
  COOLANT_LEAK = 'COOLANT_LEAK',
  STEERING_WHEEL_KNOCKS = 'STEERING_WHEEL_KNOCKS',
  STEERING_WHEEL_VIBRATION = 'STEERING_WHEEL_VIBRATION',
  STEERING_PLAY = 'STEERING_PLAY',
  GURAH_FLUID_LEAK = 'GURAH_FLUID_LEAK',
  RUDDER_WEDGE = 'RUDDER_WEDGE',
  HEAVY_STEERING_WHEEL = 'HEAVY_STEERING_WHEEL',
  LOW_BATTERY = 'LOW_BATTERY',
  DASHBOARD = 'DASHBOARD',
  HEAD_LIGHTING = 'HEAD_LIGHTING',
  REAR_LIGHTING = 'REAR_LIGHTING',
  SIGNALING = 'SIGNALING',
  PARKING_SENSORS = 'PARKING_SENSORS',
  AIR_CONDITIONER = 'AIR_CONDITIONER',
  HEATING_SYSTEM = 'HEATING_SYSTEM',
  SEAT_BELT = 'SEAT_BELT',
  SEATS = 'SEATS',
}

export const StatusNames: Record<Statuses, string> = {
  [Statuses.REPAIR_SEARCH_FOR_A_TOW_TRUCK]: 'Поиск эвакуатора',
  [Statuses.REPAIR_TOW_TRUCK_IS_COMING]: 'Эвакуатор едет к вам',
  [Statuses.REPAIR_CAR_WAS_TRANSFERRED_TO_A_TOW_TRUCK]: 'Машина передана на эвакуатор',
  [Statuses.REPAIR_REGISTRATION_AT_THE_SERVICE_STATION]: 'Запись на СТО',
  [Statuses.REPAIR_REGISTRATION_FOR_FIELD_SERVICE]: 'Запись на Выездной сервис',
  [Statuses.REPAIR_PERFORMER_ON_THE_WAY]: 'Исполнитель в пути',
  [Statuses.REPAIR_DETERMINATION_OF_THE_LIST_OF_WORKS]: 'Определение перечня работ',
  [Statuses.REPAIR_EXPECTED_AT_THE_SERVICE_STATION]: 'Вас ожидают на СТО',
  [Statuses.REPAIR_CAR_WAS_ACCEPTED_AT_THE_SERVICE_STATION]: 'Машина принята на СТО',
  [Statuses.REPAIR_DIAGNOSTICS]: 'Диагностика автомобиля',
  [Statuses.REPAIR_WORK_ORDER_APPROVAL]: 'Согласование Заказ-наряда',
  [Statuses.REPAIR_WORK]: 'Ремонтные работы',
  [Statuses.REPAIR_ISSUING_A_CAR]: 'Выдача автомобиля',
  [Statuses.REPAIR_FINISHED]: 'Завершено',
  [Statuses.REPAIR_CANCELED]: 'Отменено',
};

export const ProblemNames: Record<Problems, string> = {
  [Problems.DOORS_AND_WINDOWS]: 'Двери и окна',
  [Problems.DIAGNOSTICS]: 'Авто не заводится',
  [Problems.ENGINE]: 'Двигатель',
  [Problems.TRANSMISSION]: 'Коробка передач',
  [Problems.CLUTCH]: 'Сцепление',
  [Problems.BRAKES]: 'Тормоза',
  [Problems.CHASSIS]: 'Ходовая часть',
  [Problems.COOLING]: 'Система охлаждения',
  [Problems.STEERING]: 'Рулевое управление',
  [Problems.ELECTRICAL_EQUIPMENT]: 'Электрооборудование',
  [Problems.CAR_INTERIOR]: 'Салон авто',
  [Problems.OTHER]: 'Прочее',
  [Problems.TRAFFIC_ACCIDENT]: 'Ремонт после ДТП',
  [Problems.MAINTENANCE]: 'Техническое обслуживание',
  [Problems.TOW_TRUCK]: 'Эвакуатор',
  [Problems.WHEEL_REPAIR]: 'Шиномонтаж',
};

export const DefectNames: Record<Defects, string> = {
  [Defects.DOOR_LOCK]: 'Замок двери',
  [Defects.DOOR_CLOSING_SENSOR]: 'Датчик закрытия двери',
  [Defects.DOOR_HINGES]: 'Дверные петли',
  [Defects.POWER_WINDOW]: 'Стеклоподъемник',
  [Defects.CAR_NOT_OPEN]: 'Автомобиль не открывается',
  [Defects.CHECK_ENGINE_IS_ON]: 'Горит Check Engine',
  [Defects.OIL_LEAK]: 'Течь масла',
  [Defects.BELT_BROKE]: 'Порвался ремень',
  [Defects.EXTRANEOUS_SOUND]: 'Посторонний звук (стук)',
  [Defects.TRANSMISSION_DOES_NOT_TURN_ON]: 'Не включается передача',
  [Defects.KNOCKS_OUT_THE_TRANSMISSION]: 'Выбивает передачу',
  [Defects.CANT_TURN_ON_ALL_WHEEL_DRIVE]: 'Полный привод',
  [Defects.GRINDING_OF_PADS]: 'Скрежет колодок',
  [Defects.BRAKE_FLUID_LEAK]: 'Течь тормозной жидкости',
  [Defects.HAND_BRAKE]: 'Ручной тормоз',
  [Defects.SUSPENSION_FRONT]: 'Подвеска: передняя часть',
  [Defects.SUSPENSION_BACK]: 'Подвеска: задняя часть',
  [Defects.ENGINE_OVERHEATING]: 'Перегрев двигателя',
  [Defects.COOLANT_LEAK]: 'Течь охлаждающей жидкости',
  [Defects.RADIATOR]: 'Радиатор',
  [Defects.STEERING_WHEEL_KNOCKS]: 'Стуки руля',
  [Defects.STEERING_WHEEL_VIBRATION]: 'Вибрация руля',
  [Defects.STEERING_PLAY]: 'Люфт руля',
  [Defects.GURAH_FLUID_LEAK]: 'Течь жидкости ГУРа',
  [Defects.RUDDER_WEDGE]: 'Клин руля',
  [Defects.HEAVY_STEERING_WHEEL]: 'Тяжелый руль',
  [Defects.LOW_BATTERY]: 'Разряжен аккумулятор',
  [Defects.DASHBOARD]: 'Панель приборов',
  [Defects.HEAD_LIGHTING]: 'Головное освещение',
  [Defects.REAR_LIGHTING]: 'Заднее освещение',
  [Defects.SIGNALING]: 'Сигнализация',
  [Defects.PARKING_SENSORS]: 'Парктроник',
  [Defects.AIR_CONDITIONER]: 'Кондиционер',
  [Defects.HEATING_SYSTEM]: 'Система отопления',
  [Defects.SEAT_BELT]: 'Ремень безопасности',
  [Defects.SEATS]: 'Сидения',
};

export const DefaultStatuses = [
  Statuses.REPAIR_SEARCH_FOR_A_TOW_TRUCK,
  Statuses.REPAIR_TOW_TRUCK_IS_COMING,
  Statuses.REPAIR_CAR_WAS_TRANSFERRED_TO_A_TOW_TRUCK,
  Statuses.REPAIR_REGISTRATION_AT_THE_SERVICE_STATION,
  Statuses.REPAIR_REGISTRATION_FOR_FIELD_SERVICE,
  Statuses.REPAIR_PERFORMER_ON_THE_WAY,
  Statuses.REPAIR_DETERMINATION_OF_THE_LIST_OF_WORKS,
  Statuses.REPAIR_EXPECTED_AT_THE_SERVICE_STATION,
  Statuses.REPAIR_CAR_WAS_ACCEPTED_AT_THE_SERVICE_STATION,
  Statuses.REPAIR_DIAGNOSTICS,
  Statuses.REPAIR_WORK_ORDER_APPROVAL,
  Statuses.REPAIR_WORK,
  Statuses.REPAIR_ISSUING_A_CAR,
];

export const VehicleName: Record<string, string> = {
  brand: 'Марка авто',
  mileage: 'Пробег',
  model: 'Модель',
  stateNumber: 'Государственный номер',
  vin: 'VIN',
  year: 'Год выпуска',
};

export const FilterFieldsDefault = {
  requestStatusSet: DefaultStatuses,
  ...GeneralFilterFieldsDefault,
};

export const REPAIR = 'repair';
export const MONITORING = 'monitoring';
export const MONITORING_ID = `${MONITORING}/:orderId`;
export const REQUEST = 'request';
export const REQUEST_ID = `${REQUEST}/:orderId`;
export const ORGANIZATION = 'organization';

export const REPAIR_ORDER_DETAILED = `/engineers/monitor/${REPAIR}`;

export const GET_ORDER_LIST = `${MOCKED_API_PREFIX}/${REPAIR}/${MONITORING}`;
export const GET_ORDER = `${MOCKED_API_PREFIX}/${REPAIR}/${MONITORING_ID}`;
export const TAKE_TO_WORK = `${MOCKED_API_PREFIX}/${REPAIR}/${MONITORING_ID}/take_to_work`;
export const WORK_ORDER = `${MOCKED_API_PREFIX}/${REPAIR}/${MONITORING_ID}/work_order`;
export const CHANGE_ORDER = `${MOCKED_API_PREFIX}/${REPAIR}/${MONITORING}/${REQUEST_ID}`;
export const SAVE_WORK_ORDER_PRICE = `${MOCKED_API_PREFIX}/${REPAIR}/${MONITORING}/revision`;
export const GET_STATE_NUMBER_LIST = `${MOCKED_API_PREFIX}/${REPAIR}/vehicle/state-number?searchText=:stateNumber`;
export const GET_EMPLOYEE_LIST = `${MOCKED_API_PREFIX}/${REPAIR}/employee?searchName=:name`;
export const GET_ORGANIZATION_LIST = `${MOCKED_API_PREFIX}/${REPAIR}/organization`;
export const GET_EMPLOYEE_ORGANIZATION = `${MOCKED_API_PREFIX}/${REPAIR}/${ORGANIZATION}/employee`;
export const GET_DEPARTMENT_LIST = `${MOCKED_API_PREFIX}/${REPAIR}/${ORGANIZATION}/department`;
export const GET_DEADLINE = `${MOCKED_API_PREFIX}/${REPAIR}/${MONITORING}/deadline/${REQUEST_ID}`;

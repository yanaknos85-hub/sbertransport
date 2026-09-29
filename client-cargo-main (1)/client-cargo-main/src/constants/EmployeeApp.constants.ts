import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

export enum ServiceEnum {
  taxi = 'taxi',
  personal = 'personal',
  cargo = 'cargo',
  regularCargo = 'regularCargo',
  public = 'public',
  carsharing = 'carsharing',
  cooperative = 'cooperative',
  repair = 'repair',
  parkingPay = 'parkingPay',
}

export const ServiceEnumTitles = {
  [ServiceEnum.taxi]: 'Такси',
  [ServiceEnum.personal]: 'Личный транспорт',
  [ServiceEnum.cargo]: ' Доставка',
  [ServiceEnum.regularCargo]: 'Регулярная доставка',
  [ServiceEnum.public]: 'Общественный транспорт',
  [ServiceEnum.carsharing]: 'Каршеринг',
  [ServiceEnum.cooperative]: 'Групповые поездки',
  [ServiceEnum.repair]: 'Ремонт автомобиля',

  [ServiceEnum.parkingPay]: 'Оплатить парковку',
};

export enum EmployeeAppLinks {
  create = 'create',
  trips = 'trips',
  cargos = 'cargos',
  compensation = 'compensation',
  regularCargos = 'regularCargos',
  history = 'history',
  favorite = 'favorite',
  personalCars = 'personalCars',
  approvement = 'approvement',
  limits = 'limits',
  support = 'support',
  profile = 'profile',
  admin = 'admin',
  delegates = 'delegates',
  approvalRequestList = 'requests',
  approvementLimit = 'limit',
  limitsInfo = 'limitsInfo',
  limitRequests = 'limitRequests',
  bonuses = 'bonuses',
  bonusesAccount = 'bonusesAccount',

  taxi = 'taxi',
  personal = 'personal',
  cargo = 'cargo',
  cargoScheduler = 'cargoScheduler',
  cargoMass = 'cargoMass',
  public = 'public',
  carsharing = 'carsharing',
  cooperative = 'cooperative',
  single = 'single',
  regular = 'regular',
  list = 'list',
}

export const EmployeeAppLinksTitles = {
  [EmployeeAppLinks.create]: 'Оформить поездку',
  [EmployeeAppLinks.trips]: 'Поездки',
  [EmployeeAppLinks.cargos]: 'Доставки',
  [EmployeeAppLinks.regularCargos]: 'Регулярные доставки',
  [EmployeeAppLinks.approvement]: 'Мои согласования',
  [EmployeeAppLinks.limits]: 'Мои лимиты',
  [EmployeeAppLinks.personalCars]: 'Мой транспорт',
  [EmployeeAppLinks.favorite]: 'Мои адреса',
  [EmployeeAppLinks.support]: 'Поддержка',
  [EmployeeAppLinks.profile]: 'Профиль',
  [EmployeeAppLinks.admin]: 'Переход в корпоративный кабинет',
  [EmployeeAppLinks.delegates]: 'Делегаты',
  [EmployeeAppLinks.approvalRequestList]: 'Заявки на поездки',
  [EmployeeAppLinks.approvementLimit]: 'Заявки на лимиты',
  [EmployeeAppLinks.limitsInfo]: 'Лимиты',
  [EmployeeAppLinks.limitRequests]: 'Заявки на лимиты',
  [EmployeeAppLinks.bonuses]: 'Бонусы',
  [EmployeeAppLinks.bonusesAccount]: 'Бонусный счет',

  [EmployeeAppLinks.taxi]: 'Такси',
  [EmployeeAppLinks.personal]: 'Личный транспорт',
  [EmployeeAppLinks.cargo]: 'Доставка',
  [EmployeeAppLinks.cargoMass]: 'Экспорт из Excel',
  [EmployeeAppLinks.public]: 'Общественный транспорт',
  [EmployeeAppLinks.carsharing]: 'Каршеринг',
  [EmployeeAppLinks.cooperative]: 'Групповые поездки',
};

export enum EmployeeStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
}

export enum EmployeeStatusTitle {
  ACTIVE = 'Активный',
  INACTIVE = 'Неактивный',
}

export type EmployeeStatusType = keyof typeof EmployeeStatus;

export const IOEmployeeStatusType = ioTypeFromEnum<EmployeeStatusType>('employeeStatusType', EmployeeStatus);

export enum TripsTabsFilters {
  planned = 'planned',
  final = 'final',
}

export type TTripsTabsFilters = keyof typeof TripsTabsFilters;

export enum LimitRequestTabsFilters {
  active = 'active',
  final = 'final',
}

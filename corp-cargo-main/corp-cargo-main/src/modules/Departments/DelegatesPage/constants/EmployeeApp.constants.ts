import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

export enum ServiceTypeEnum {
  EMPLOYEE_TRANSPORTATION = 'EMPLOYEE_TRANSPORTATION',
  CARGO_TRANSPORTATION = 'CARGO_TRANSPORTATION',
  REPAIR = 'REPAIR',
}

export const ServiceTypeEnumTitles = {
  [ServiceTypeEnum.EMPLOYEE_TRANSPORTATION]: 'Перевозка сотрудников',
  [ServiceTypeEnum.CARGO_TRANSPORTATION]: 'Грузоперевозки',
  [ServiceTypeEnum.REPAIR]: 'Ремонт',
};

export enum ServiceEnum {
  taxi = 'taxi',
  personal = 'personal',
  cargo = 'cargo',
  regularCargo = 'regularCargo',
  public = 'public',
  carsharing = 'carsharing',
  cooperative = 'cooperative',
  maintenance = 'maintenance',
  parkingPay = 'parkingPay',
  quotaManagement = 'quotaManagement',
}

export const ServiceEnumTitles = {
  [ServiceEnum.taxi]: 'Такси',
  [ServiceEnum.personal]: 'Личный транспорт',
  [ServiceEnum.cargo]: ' Доставка',
  [ServiceEnum.regularCargo]: 'Регулярная доставка',
  [ServiceEnum.public]: 'Общественный транспорт',
  [ServiceEnum.carsharing]: 'Каршеринг',
  [ServiceEnum.cooperative]: 'Групповые поездки',
  [ServiceEnum.maintenance]: 'Автосервис',
  [ServiceEnum.parkingPay]: 'Оплатить парковку',
  [ServiceEnum.quotaManagement]: 'Управление квотами',
};

export enum EmployeeAppLinks {
  create = 'create',
  trips = 'trips',
  cargos = 'cargos',
  regularCargos = 'regularCargos',
  history = 'history',
  favorite = 'favorite',
  personalCars = 'personalCars',
  approvement = 'approvement',
  limits = 'limits',
  requests = 'requests',
  support = 'support',
  profile = 'profile',
  admin = 'admin',
  delegates = 'delegates',
  // eslint-disable-next-line @typescript-eslint/no-duplicate-enum-values
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
}

export const EmployeeAppLinksTitles = {
  [EmployeeAppLinks.create]: 'Оформить поездку',
  [EmployeeAppLinks.trips]: 'Поездки',
  [EmployeeAppLinks.cargos]: 'Доставки',
  [EmployeeAppLinks.regularCargos]: 'Регулярная доставки',
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

export enum CargosTabsFilters {
  active = 'active',
  final = 'final',
}

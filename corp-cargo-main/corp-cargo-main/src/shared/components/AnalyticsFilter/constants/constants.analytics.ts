export enum Titles {
  report = 'Отчетность',
  corpClient = 'Корпоративный клиент',
  seeStatistics = 'Период',
  services = 'Сервисы',
  serviceType = 'Услуги',
}

export enum ServiceTypes {
  all = 'all',
  passengerTransportation = 'passengerTransportation',
  cargoTransportation = 'cargoTransportation',
  fleetManagement = 'fleetManagement',
}

export enum ServiceTypeTitles {
  all = 'Все',
  passengerTransportation = 'Пассажирские перевозки',
  cargoTransportation = 'Грузоперевозки',
  fleetManagement = 'Автосервис',
}

export enum TransportTypesPassenger {
  TAXI = 'TAXI',
  PERSONAL = 'PERSONAL',
  PUBLIC = 'PUBLIC',
  CARSHARING = 'CARSHARING',
}

export enum TransportTypesCargo {
  COURIER = 'COURIER',
  DEDICATED = 'DEDICATED',
  INTERREGIONAL = 'INTERREGIONAL',
  DOMESTIC_COURIER = 'DOMESTIC_COURIER',
}

export enum FleetManagementTypes {
  OFFICIAL = 'OFFICIAL',
  PRIVATE = 'PRIVATE',
  SPECIAL = 'SPECIAL',
}

export const TransportTypePassengerTitles: Record<TransportTypesPassenger, string> = {
  [TransportTypesPassenger.TAXI]: 'Такси',
  [TransportTypesPassenger.PERSONAL]: 'Личный',
  [TransportTypesPassenger.PUBLIC]: 'Общественный',
  [TransportTypesPassenger.CARSHARING]: 'Каршеринг',
};

export const TransportTypeCargoTitles: Record<TransportTypesCargo, string> = {
  [TransportTypesCargo.COURIER]: 'Курьер',
  [TransportTypesCargo.DEDICATED]: 'Доставка сборного груза',
  [TransportTypesCargo.INTERREGIONAL]: 'Межрегиональная',
  [TransportTypesCargo.DOMESTIC_COURIER]: 'Внутренний курьер',
};

export const FleetManagementTitles: Record<FleetManagementTypes, string> = {
  [FleetManagementTypes.OFFICIAL]: 'Служебный',
  [FleetManagementTypes.PRIVATE]: 'Персональный',
  [FleetManagementTypes.SPECIAL]: 'Специальный',
};

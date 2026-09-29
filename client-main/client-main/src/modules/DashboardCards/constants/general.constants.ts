export enum ServiceType {
  ALL = 'ALL',
  PASSENGERS = 'PASSENGERS',
  CARGO = 'CARGO',
  MAINTENANCE = 'MAINTENANCE',
  PARKING = 'PARKING',
}

export enum QuickOrderServiceEnum {
  TAXI = 'TAXI',
  PUBLIC = 'PUBLIC',
  PERSONAL = 'PERSONAL',
  CARSHARING = 'CARSHARING',
  TRANSFER = 'TRANSFER',
  BUS = 'BUS',
}

export const QuickOrderServiceTitle = {
  [QuickOrderServiceEnum.TAXI]: 'Такси',
  [QuickOrderServiceEnum.PERSONAL]: 'Личный',
  [QuickOrderServiceEnum.PUBLIC]: 'Общественный',
  [QuickOrderServiceEnum.CARSHARING]: 'Каршеринг',
  [QuickOrderServiceEnum.TRANSFER]: 'Трансфер',
  [QuickOrderServiceEnum.BUS]: 'Автобус',
};

export const QuickOrderServiceLinks = {
  [QuickOrderServiceEnum.TAXI]: `passengers/trips/create/taxi`,
  [QuickOrderServiceEnum.PUBLIC]: `passengers/trips/create/public`,
  [QuickOrderServiceEnum.PERSONAL]: `passengers/trips/create/personal`,
  [QuickOrderServiceEnum.CARSHARING]: `passengers/trips/create/carsharing`,
  [QuickOrderServiceEnum.TRANSFER]: `passengers/trips/create/transfer`,
  [QuickOrderServiceEnum.BUS]: `passengers/trips/create/bus`,
};

export enum MFServiceLinks {
  taxiCreate = 'taxiCreate',
  personalCreate = 'personalCreate',
  publicCreate = 'publicCreate',
  carsharingCreate = 'carsharingCreate',
  cooperativeCreate = 'cooperativeCreate',
  cargoCreate = 'cargoCreate',
  cargoCreateMulti = 'cargoCreateMulti',
  regularCargoCreate = 'regularCargoCreate',
  maintenance = 'maintenance',
  parking = 'parking',
  transportCreate = 'transportCreate',
}

export const DashboardLinks = {
  [MFServiceLinks.transportCreate]: `passengers/trips/create/transport2.0`,
  [MFServiceLinks.taxiCreate]: `passengers/trips/create/taxi`,
  [MFServiceLinks.personalCreate]: `passengers/trips/create/personal`,
  [MFServiceLinks.publicCreate]: `passengers/trips/create/public`,
  [MFServiceLinks.carsharingCreate]: `passengers/trips/create/carsharing`,
  [MFServiceLinks.cooperativeCreate]: `passengers/trips/create/cooperative`,
  [MFServiceLinks.cargoCreate]: `cargo/single/create`,
  [MFServiceLinks.cargoCreateMulti]: `cargo/multiple/create`,
  [MFServiceLinks.regularCargoCreate]: `cargo/regular/create`,
  [MFServiceLinks.maintenance]: `fleet/order`,
  [MFServiceLinks.parking]: `fleet/parking`,
};

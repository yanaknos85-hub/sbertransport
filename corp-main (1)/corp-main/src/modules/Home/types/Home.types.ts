export enum Buttons {
  MANAGE_CONTRACT_BASE = 'MANAGE_CONTRACT_BASE',
  BUDGET = 'BUDGET',
  TARIFFS_SETTINGS = 'TARIFFS_SETTINGS',
  SERVICE_PARAMS = 'SERVICE_PARAMS',
  INDICATORS = 'INDICATORS',
}

export enum Widgets {
  BUDGET = 'BUDGET',
  INDICATORS = 'INDICATORS',
}

export interface QueryType {
  organizationId: string;
  year: number;
  transportTypes: string[];
}

export enum ServicesTypes {
  ALL = 'ALL',
  PASSENGERS = 'PASSENGERS',
  CARGO = 'CARGO',
  REPAIR = 'REPAIR',
}

export enum TransportTypes {
  DEDICATED = 'DEDICATED',
  COURIER = 'COURIER',
  DOMESTIC_COURIER = 'DOMESTIC_COURIER',
  INTERREGIONAL = 'INTERREGIONAL',
  OFFICIAL = 'OFFICIAL',
  SPECIAL = 'SPECIAL',
  PRIVATE = 'PRIVATE',
  CARSHARING = 'CARSHARING',
  PUBLIC = 'PUBLIC',
  TAXI = 'TAXI',
  WALK = 'WALK',
  PERSONAL = 'PERSONAL',
  BICYCLE = 'BICYCLE',
}

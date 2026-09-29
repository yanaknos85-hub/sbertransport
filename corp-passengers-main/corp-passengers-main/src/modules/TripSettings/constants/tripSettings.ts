import { Dispatch, SetStateAction } from 'react';
import { StepItemContentProps } from 'shared/components/Steps';

export enum Steps {
  ServiceTypes = 'service-types',
  Purposes = 'purposes',
  Approvals = 'approvals',
  SharedRides = 'shared-rides',
}

export enum ServicesTabs {
  Passengers = 'passengers',
  Cargo = 'cargo',
  Fleet = 'fleet',
}

export enum TripPurposesTabs {
  Passengers = 'passengers',
  Cargo = 'cargo',
}

export enum ApprovalsTabs {
  Passengers = 'passengers',
  Cargo = 'cargo',
}

export enum SharedRidesTabs {
  Passengers = 'passengers',
  Cargo = 'cargo',
}

export enum serviceTypes {
  EMPLOYEE_TRANSPORTATION = 'EMPLOYEE_TRANSPORTATION',
  CARGO_TRANSPORTATION = 'CARGO_TRANSPORTATION',
  FLEET_MANAGEMENT = 'FLEET_MANAGEMENT',
}

export const serviceTypesTitles: Record<serviceTypes, string> = {
  [serviceTypes.EMPLOYEE_TRANSPORTATION]: 'Пассажирские перевозки',
  [serviceTypes.CARGO_TRANSPORTATION]: 'Грузовые перевозки',
  [serviceTypes.FLEET_MANAGEMENT]: 'Управление автопарком',
};

export interface TransportTypeRecord {
  transportType: string;
  active: boolean;
  rusName: string;
  category: string;
}

export enum TransportTypes {
  TAXI = 'TAXI',
  PERSONAL = 'PERSONAL',
  PUBLIC = 'PUBLIC',
  CARSHARING = 'CARSHARING',
  BICYCLE = 'BICYCLE',
  WALK = 'WALK',
  SCOOTER = 'SCOOTER',
  COURIER = 'COURIER',
  DEDICATED = 'DEDICATED',
  INDIVIDUAL = 'INDIVIDUAL',
  INTERREGIONAL = 'INTERREGIONAL',
  DOMESTIC_COURIER = 'DOMESTIC_COURIER',
  OFFICIAL = 'OFFICIAL',
  SPECIAL = 'SPECIAL',
  PRIVATE = 'PRIVATE',
  GROUP_TRANSFER = 'GROUP_TRANSFER',
}

export const TransportTypeCategories: Record<TransportTypes, string> = {
  [TransportTypes.TAXI]: serviceTypes.EMPLOYEE_TRANSPORTATION,
  [TransportTypes.PERSONAL]: serviceTypes.EMPLOYEE_TRANSPORTATION,
  [TransportTypes.PUBLIC]: serviceTypes.EMPLOYEE_TRANSPORTATION,
  [TransportTypes.BICYCLE]: serviceTypes.EMPLOYEE_TRANSPORTATION,
  [TransportTypes.WALK]: serviceTypes.EMPLOYEE_TRANSPORTATION,
  [TransportTypes.SCOOTER]: serviceTypes.EMPLOYEE_TRANSPORTATION,
  [TransportTypes.CARSHARING]: serviceTypes.EMPLOYEE_TRANSPORTATION,
  [TransportTypes.GROUP_TRANSFER]: serviceTypes.EMPLOYEE_TRANSPORTATION,
  [TransportTypes.COURIER]: serviceTypes.CARGO_TRANSPORTATION,
  [TransportTypes.DEDICATED]: serviceTypes.CARGO_TRANSPORTATION,
  [TransportTypes.INDIVIDUAL]: serviceTypes.CARGO_TRANSPORTATION,
  [TransportTypes.INTERREGIONAL]: serviceTypes.CARGO_TRANSPORTATION,
  [TransportTypes.DOMESTIC_COURIER]: serviceTypes.CARGO_TRANSPORTATION,
  [TransportTypes.OFFICIAL]: serviceTypes.FLEET_MANAGEMENT,
  [TransportTypes.SPECIAL]: serviceTypes.FLEET_MANAGEMENT,
  [TransportTypes.PRIVATE]: serviceTypes.FLEET_MANAGEMENT,
};

export const TransportTypeRuTitles: Record<TransportTypes, string> = {
  [TransportTypes.TAXI]: 'Такси',
  [TransportTypes.PERSONAL]: 'Личный транспорт',
  [TransportTypes.PUBLIC]: 'Общественный',
  [TransportTypes.BICYCLE]: 'Велосипед',
  [TransportTypes.WALK]: 'Пешком',
  [TransportTypes.SCOOTER]: 'Самокат',
  [TransportTypes.CARSHARING]: 'Каршеринг',
  [TransportTypes.GROUP_TRANSFER]: 'Трансфер',
  [TransportTypes.COURIER]: 'Курьер',
  [TransportTypes.DEDICATED]: 'Доставка сборного груза',
  [TransportTypes.INDIVIDUAL]: 'Доставка выделенным транспортом',
  [TransportTypes.INTERREGIONAL]: 'Межрегиональная доставка',
  [TransportTypes.DOMESTIC_COURIER]: 'Внутренний курьер',
  [TransportTypes.OFFICIAL]: 'Служебный',
  [TransportTypes.SPECIAL]: 'Специальный',
  [TransportTypes.PRIVATE]: 'Персональный',
};

export interface NSave {
  saveRequired: boolean;
  saveLocation: string;
}

export type TProps = StepItemContentProps & {
  needSave: NSave;
  setNeedSave: Dispatch<SetStateAction<NSave>>;
};

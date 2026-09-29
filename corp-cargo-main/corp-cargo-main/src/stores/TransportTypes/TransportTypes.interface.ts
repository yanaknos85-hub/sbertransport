import * as t from 'io-ts';

export enum TransportTypes {
  TAXI = 'TAXI',
  PUBLIC = 'PUBLIC',
  PERSONAL = 'PERSONAL',
  CARSHARING = 'CARSHARING',
  BICYCLE = 'BICYCLE',
  WALK = 'WALK',
  SCOOTER = 'SCOOTER',
  DEDICATED = 'DEDICATED',
  INDIVIDUAL = 'INDIVIDUAL',
  COURIER = 'COURIER',
  INTERREGIONAL = 'INTERREGIONAL',
  DOMESTIC_COURIER = 'DOMESTIC_COURIER',
}

export const TransportTypeDescriptions: Record<TransportTypes, string> = {
  TAXI: 'Такси',
  PUBLIC: 'Общественный',
  PERSONAL: 'Личный',
  CARSHARING: 'Каршеринг',
  BICYCLE: 'Велосипед',
  WALK: 'Пешком',
  SCOOTER: 'Самокат',
  DEDICATED: 'Доставка сборного груза',
  INDIVIDUAL: 'Доставка выделенным транспортом',
  COURIER: 'Курьер',
  INTERREGIONAL: 'Межрегиональная',
  DOMESTIC_COURIER: 'Внутренний курьер',
};

export const TransportTypeLongDescriptions: Record<TransportTypes, string> = {
  TAXI: 'Такси',
  PUBLIC: 'Общественный транспорт',
  PERSONAL: 'Личный транспорт',
  CARSHARING: 'Каршеринг',
  BICYCLE: 'Велосипед',
  WALK: 'Пешком',
  SCOOTER: 'Самокат',
  DEDICATED: 'Доставка сборного груза',
  INDIVIDUAL: 'Доставка выделенным транспортом',
  COURIER: 'Курьер',
  INTERREGIONAL: 'Межрегиональная',
  DOMESTIC_COURIER: 'Внутренний курьер',
};

export enum TransportTypesCargo {
  DEDICATED = 'DEDICATED',
  COURIER = 'COURIER',
  INTERREGIONAL = 'INTERREGIONAL',
  DOMESTIC_COURIER = 'DOMESTIC_COURIER',
  INDIVIDUAL = 'INDIVIDUAL',
}

export const TransportTypeCargoDescriptions: Record<TransportTypesCargo, string> = {
  DEDICATED: 'Доставка сборного груза',
  COURIER: 'Курьер',
  INTERREGIONAL: 'Межрегиональная',
  DOMESTIC_COURIER: 'Внутренний курьер',
  INDIVIDUAL: 'Доставка выделенным транспортом',
};

export const OrganizationTransportTypes = t.strict({
  transportType: t.string,
  active: t.boolean,
});

export type OrganizationTransportTypes = t.TypeOf<typeof OrganizationTransportTypes>;

export const TransportType = t.strict({
  id: t.string,
  name: t.string,
  rusName: t.string,
});

export type TransportType = t.TypeOf<typeof TransportType>;


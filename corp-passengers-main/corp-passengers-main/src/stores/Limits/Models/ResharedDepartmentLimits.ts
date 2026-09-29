import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

export enum TRANSPORT_TYPE {
  TAXI = 'TAXI',
  PERSONAL = 'PERSONAL',
  PUBLIC = 'PUBLIC',
  CARSHARING = 'CARSHARING',
  BICYCLE = 'BICYCLE',
  WALK = 'WALK',
  SCOOTER = 'SCOOTER',
  DEDICATED = 'DEDICATED',
  INDIVIDUAL = 'INDIVIDUAL',
  COURIER = 'COURIER',
  INTERREGIONAL = 'INTERREGIONAL',
  DOMESTIC_COURIER = 'DOMESTIC_COURIER',
  GROUP_TRANSFER = 'GROUP_TRANSFER',
  BUS = 'BUS',
}

export const TransportTypeDescriptions: Record<TRANSPORT_TYPE, string> = {
  TAXI: 'Такси',
  PERSONAL: 'Личный',
  PUBLIC: 'Общественный',
  CARSHARING: 'Каршеринг',
  BICYCLE: 'Велосипед',
  WALK: 'Пешком',
  SCOOTER: 'Самокат',
  DEDICATED: 'Доставка сборного груза',
  INDIVIDUAL: 'Доставка выделенным транспортом',
  COURIER: 'Курьер',
  INTERREGIONAL: 'Межрегиональная',
  DOMESTIC_COURIER: 'Внутренний курьер',
  GROUP_TRANSFER: 'Групповой трансфер',
  BUS: 'Автобусные перевозки',
};

export const transportType = ioTypeFromEnum<TRANSPORT_TYPE>('parentTransportType', TRANSPORT_TYPE);

export type transportType = t.TypeOf<typeof transportType>;

export const ResharedDepartmentLimit = t.strict({
  parentLimitId: tt.uuid,
  childDepartmentId: tt.uuid,
  parentTransportType: transportType,
  childTransportType: transportType,
  sum: t.number,
  isEditable: t.boolean,
});

export type ResharedDepartmentLimit = t.TypeOf<typeof ResharedDepartmentLimit>;

import * as t from 'io-ts';
import { ioTypeFromEnum } from '../../utils/ioTypeFromEnum';
import { TRANSPORT_TYPE } from '../Limits/Models/ResharedDepartmentLimits';

export enum AddressTypes {
  DEPARTURE_ADDRESS = 'DEPARTURE_ADDRESS',
  DESTINATION_ADDRESS = 'DESTINATION_ADDRESS',
  WAYPOINT_ADDRESS = 'WAYPOINT_ADDRESS',
}

export const AddressTypeDescriptions: Record<AddressTypes, string> = {
  DEPARTURE_ADDRESS: 'дата создания',
  DESTINATION_ADDRESS: 'желаемая дата отправления',
  WAYPOINT_ADDRESS: 'старта поездки',
};

export const addressType = ioTypeFromEnum<TRANSPORT_TYPE>('parentAddressType', TRANSPORT_TYPE);

export type addressType = t.TypeOf<typeof addressType>;

export const AddressType = t.strict({
  id: t.string,
  name: t.string,
  rusName: t.string,
});

export type AddressType = t.TypeOf<typeof AddressType>;

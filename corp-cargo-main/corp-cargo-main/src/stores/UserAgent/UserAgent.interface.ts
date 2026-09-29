import { ClientTypes, Devices, OperatingSystems } from 'constants/constants.app';
import * as t from 'io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import * as tt from '../../utils/io-ts';

export const ClientTypeQuantity = t.type({
  all: t.number,
  sudir: t.number,
  basic: t.number,
  operatingSystem: tt.nullable(ioTypeFromEnum<OperatingSystems>('OperatingSystems', OperatingSystems)),
});

export const Quantity = t.partial({
  ALL: t.array(ClientTypeQuantity),
  [ClientTypes.Client]: t.array(ClientTypeQuantity),
  [ClientTypes.ClientMobile]: t.array(ClientTypeQuantity),
  [ClientTypes.Dispatcher]: t.array(ClientTypeQuantity),
  [ClientTypes.Driver]: t.array(ClientTypeQuantity),
  [ClientTypes.Parking]: t.array(ClientTypeQuantity),
  [ClientTypes.WebCorp]: t.array(ClientTypeQuantity),
});

export const QuantityFilters = t.intersection([
  t.type({
    startDate: t.string,
    endDate: t.string,
  }),
  t.partial({
    clientType: t.string,
    device: ioTypeFromEnum<Devices>('Devices', Devices),
    updater: t.number,
  }),
]);

export type Quantity = t.TypeOf<typeof Quantity>;
export type QuantityFilters = t.TypeOf<typeof QuantityFilters>;

import { TelemechanicOrderStatus } from './onTheLineSwitch.constants';
import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

export const telemechanicOrderStatus = ioTypeFromEnum<TelemechanicOrderStatus>('TelemechanicOrderStatus', TelemechanicOrderStatus);

export const Transport = t.type({
  id: t.string,
  stateNumber: t.string,
  brand: t.string,
  model: t.string,
});

export const OnTheLineResponse = t.type({
  ewbPath: t.boolean,
  ewbId: tt.nullable(t.string),
  requestStatus: tt.nullable(telemechanicOrderStatus),
  odometerOut: tt.nullable(t.number),
  qrCode: t.boolean,
  transport: tt.nullable(Transport),
});

export type OnTheLineResponse = t.TypeOf<typeof OnTheLineResponse>;

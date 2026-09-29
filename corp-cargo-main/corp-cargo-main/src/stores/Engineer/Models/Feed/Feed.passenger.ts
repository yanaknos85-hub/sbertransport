import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

export const Passenger = t.intersection([
  t.type({
    id: tt.uuid,
    organizationId: tt.nullable(tt.uuid),
    positionId: tt.nullable(tt.uuid),
    departmentId: tt.nullable(tt.uuid),
  }),
  t.partial({
    humanReadableId: tt.nullable(t.string),
    userId: tt.nullable(t.string),
    firstName: tt.nullable(t.string),
    lastName: tt.nullable(t.string),
    patronymic: tt.nullable(t.string),
    personnelNumber: tt.nullable(t.string),
    itinerantType: tt.nullable(t.string),
    mvz: tt.nullable(t.string),
    costCenter: tt.nullable(t.string),
    marriageCertificateNumber: tt.nullable(t.string),
    delegatedById: tt.nullable(t.string),
    supervisorId: tt.nullable(t.string),
    positionName: tt.nullable(t.string),
    departmentName: tt.nullable(t.string),
    phone: tt.nullable(t.string),
    mobilePhone: tt.nullable(t.string),
  }),
]);

export type Passenger = t.TypeOf<typeof Passenger>;

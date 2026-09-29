import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

export const ApprovedBy = t.type({
  costCenter: tt.nullable(t.string),
  departmentId: tt.nullable(t.string),
  firstName: tt.nullable(t.string),
  humanReadableId: tt.nullable(t.string),
  id: tt.nullable(t.string),
  itinerantType: tt.nullable(t.string),
  lastName: tt.nullable(t.string),
  marriageCertificateNumber: tt.nullable(t.string),
  organizationId: tt.nullable(t.string),
  patronymic: tt.nullable(t.string),
  personnelNumber: tt.nullable(t.string),
  phone: tt.nullable(t.string),
  positionId: tt.nullable(t.string),
  userId: tt.nullable(t.string),
});

export type ApprovedBy = t.TypeOf<typeof ApprovedBy>;

import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { createPagination } from 'utils/io-ts/pagination';

const DriverContent = t.type({
  driver: t.type({
    id: tt.uuid,
    personnelNumber: t.string,
    fullName: t.string,
    organizationName: t.string,
    departmentName: t.string,
    departmentId: tt.uuid,
    tin: tt.nullable(t.number),
  }),
  drivingLicense: t.type({
    id: tt.uuid,
    series: t.string,
    number: t.number,
    issueDate: t.number,
  }),
});
export type TDriverContent = t.TypeOf<typeof DriverContent>;

export const TelemechanicDrivers = createPagination(DriverContent);
export type TelemechanicDrivers = t.TypeOf<typeof TelemechanicDrivers>;

export interface TelemechanicDriversFilters {
  searchText: string;
  transportId?: tt.UUID;
}

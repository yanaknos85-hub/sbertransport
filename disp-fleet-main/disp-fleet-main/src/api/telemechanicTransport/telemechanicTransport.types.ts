import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { createPagination } from 'utils/io-ts/pagination';

export const StateNumberSearchOne = t.type({
  id: tt.uuid,
  stateNumber: t.string,
  brand: t.string,
  model: t.string,
  transportType: t.string,
});
export type StateNumberSearchOne = t.TypeOf<typeof StateNumberSearchOne>;

export const TelemechanicTransport = createPagination(StateNumberSearchOne);
export type TelemechanicTransport = t.TypeOf<typeof TelemechanicTransport>;

export interface StateNumberSearchOneFilters {
  stateNumber: string;
  organizationId?: tt.UUID;
  departmentId?: tt.UUID;
}

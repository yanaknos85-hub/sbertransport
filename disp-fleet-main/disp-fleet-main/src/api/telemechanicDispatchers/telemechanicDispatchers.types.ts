import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

export const DispatcherOrganization = t.type({
  id: tt.uuid,
  name: t.string,
  tariffDepartmentId: tt.uuid,
  msrn: t.string,
  tin: t.string,
  phone: t.string,
});

export const DispatcherDepartment = t.type({
  id: tt.uuid,
});

export const SelfOrganizationDispatcherResponse = t.type({
  dispatcherId: tt.uuid,
  regionCode: t.string,
  organization: DispatcherOrganization,
  department: DispatcherDepartment,
});
export type TSelfOrganizationDispatcherResponse = t.TypeOf<typeof SelfOrganizationDispatcherResponse>;

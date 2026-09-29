import { EmployeeStatus, OrgStructureType } from 'constants/constants.app';
import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

export const Employee = t.intersection([
  t.type({
    humanReadableId: t.string,
    id: tt.uuid,
    firstName: t.string,
    lastName: t.string,
    organizationId: tt.uuid,
    departmentId: tt.uuid,
    positionId: tt.uuid,
    attributes: t.array(t.unknown),
    status: t.keyof(EmployeeStatus),
  }),
  t.partial({
    organizationName: t.string,
    departmentName: t.string,
    availableTransportTypes: t.UnknownArray,
    userId: t.string,
    personnelNumber: t.string,
    roles: t.array(t.string),
    patronymic: t.string,
    mobilePhone: t.string,
    email: t.string,
    supervisorId: t.string,
    delegatedById: t.string,
    personalCars: t.UnknownArray,
    positionName: t.string,
    gender: t.string,
    isOrganization: t.boolean,
    executorGroupId: t.array(t.string),
    orgStructureType: ioTypeFromEnum<OrgStructureType>('OrgStructureType', OrgStructureType),
  }),
]);
export type Employee = t.TypeOf<typeof Employee>;

export const SelfEmployee = t.intersection([
  Employee,
  t.type({
    consent: t.boolean,
  }),
]);
export type SelfEmployee = t.TypeOf<typeof SelfEmployee>;

export const RegionInfoType = t.intersection([
  t.type({
    id: t.string, name: t.string, code: t.union([t.string, t.number]),
  }),
  t.partial({
    parent_id: tt.uuid,
  }),
]);

export type RegionInfoType = t.TypeOf<typeof RegionInfoType>;


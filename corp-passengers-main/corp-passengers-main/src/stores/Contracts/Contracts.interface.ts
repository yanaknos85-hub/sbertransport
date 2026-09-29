import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { createPagination, PaginationParams } from 'stores/Pagination/Pagination.interface';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import { ContractRestrictionTypes, ContractTypes } from 'constants/constants.app';

export const Contract = t.intersection([
  t.strict({
    id: tt.uuid,
    organizationIds: tt.optional(t.array(tt.uuid)),
    contractorId: tt.uuid,
    transportType: t.string,
    applyTo: t.union([t.array(tt.uuid), t.undefined]),
    startDate: t.string,
    endDate: tt.optional(t.string),
    sum: t.number,
    active: t.boolean,
    contractNumber: t.string,
    includeVat: t.boolean,
    vatValue: tt.optional(t.number),
    regionIds: t.array(t.string),
  }),
  t.partial({
    // TODO: Не соответсвует swagger. Пинать бек
    region: t.string,
    autoPlanning: t.boolean,
    uvhd: t.string,
    restrictionType: ioTypeFromEnum<ContractRestrictionTypes>('ContractRestrictionTypes', ContractRestrictionTypes),
    organizationNames: t.array(t.string),
    connectedContracts: t.array(t.string),
    driverLatePickupPenalty: t.number,
    poorServiceQualityPenalty: t.number,
    driverOrderCancellationPenalty: t.number,
    responsibleEmployeeId: tt.uuid,
    responsibleEmployeeName: t.string,
    paymentOrganizationId: tt.uuid,
  }),
]);

export type Contract = t.TypeOf<typeof Contract>;

// export const CarServiceContract = t.type({
//   id: tt.uuid,
//   contractorId: tt.uuid,
//   contractorName: t.string,
//   amount: t.number,
//   start: t.string,
//   end: tt.nullable(t.string),
//   active: t.boolean,
//   number: t.string,
//   uvhd: tt.nullable(t.string),
// });

// export type CarServiceContract = t.TypeOf<typeof CarServiceContract>;

export const ContractsSearchResponse = createPagination(Contract);

export type ContractsSearchResponse = t.TypeOf<typeof ContractsSearchResponse>;

// export const CarServiceContractSearchResponse = createPagination(CarServiceContract);

// export type CarServiceContractSearchResponse = t.TypeOf<typeof CarServiceContractSearchResponse>;

export const ContractsSearchQuery = t.partial({
  serviceType: t.string,
  transportType: t.string,
  region: tt.nullable(t.string),
  contractorId: t.string,
  contractNumber: t.string,
  date: t.string,
  organizationId: t.string,
  active: t.string,
  pagination: PaginationParams,
  contractType: ioTypeFromEnum<ContractTypes>('ContractTypes', ContractTypes),
});

export type ContractsSearchQuery = t.TypeOf<typeof ContractsSearchQuery>;

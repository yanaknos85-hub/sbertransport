import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { createPagination, PaginationParams } from 'stores/Pagination/Pagination.interface';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import { ContractRestrictionTypes, ContractTypes } from 'constants/constants.app';

export const Settings = t.type({
  type: t.string,
  value: t.string,
});

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
    includeTemplate: t.boolean,
    includePurpose: t.boolean,
    vatValue: tt.optional(t.number),
    templateValue: tt.optional(t.number),
    regionIds: t.array(t.string),
    settings: t.array(Settings),
  }),
  t.partial({
    // TODO: Не соответсвует swagger. Пинать бек
    region: t.string,
    autoPlanning: t.boolean,
    uvhd: t.string,
    restrictionType: ioTypeFromEnum<ContractRestrictionTypes>('ContractRestrictionTypes', ContractRestrictionTypes),
    organizationNames: t.array(t.string),
  }),
]);

export type Contract = t.TypeOf<typeof Contract>;

export const CarServiceContract = t.type({
  id: tt.uuid,
  contractorId: tt.uuid,
  contractorName: t.string,
  amount: t.number,
  start: t.string,
  end: tt.nullable(t.string),
  active: t.boolean,
  number: t.string,
  uvhd: tt.nullable(t.string),
});

export type CarServiceContract = t.TypeOf<typeof CarServiceContract>;

export const ContractsSearchResponse = createPagination(Contract);

export type ContractsSearchResponse = t.TypeOf<typeof ContractsSearchResponse>;

export const CarServiceContractSearchResponse = createPagination(CarServiceContract);

export type CarServiceContractSearchResponse = t.TypeOf<typeof CarServiceContractSearchResponse>;

export const ContractsSearchQuery = t.partial({
  serviceType: t.string,
  transportType: t.string,
  region: tt.nullable(t.string),
  // Поле использутеся при создании/обновлении тарифа
  regionIds: t.union([t.array(t.string), t.null]),
  contractorId: t.string,
  contractNumber: t.string,
  date: t.string,
  organizationId: t.string,
  active: t.string,
  pagination: PaginationParams,
  contractType: ioTypeFromEnum<ContractTypes>('ContractTypes', ContractTypes),
});

export type ContractsSearchQuery = t.TypeOf<typeof ContractsSearchQuery>;

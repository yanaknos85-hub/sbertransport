import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { createPagination } from 'utils/io-ts/pagination';

export const ContractorDispatcherBase = t.intersection([
  t.type({
    firstName: t.string,
    lastName: t.string,
    id: tt.uuid,
  }),
  t.partial({
    patronymic: t.string,
  }),
]);

export const ContractorDispatcher = t.intersection([
  ContractorDispatcherBase,
  t.type({
    phone: t.string,
    email: t.string,
    contractorId: tt.uuid,
    humanReadableId: t.string,
  }),
  t.partial({
    autoparkId: tt.uuid,
  }),
]);

export const ContractorDispatcherResponse = createPagination(ContractorDispatcher);

export const ContractorDispatcherSelf = t.intersection([
  ContractorDispatcher,
  t.type({
    consent: t.boolean,
    organizationId: t.string,
    departmentId: t.string,
    ewbCreationPossibility: t.boolean,
  }),
  t.partial({
    oauthId: t.string,
    originAutoparkId: t.string,
  }),
]);

export type ContractorDispatcherSelf = t.TypeOf<typeof ContractorDispatcherSelf>;

export const PaginationParams = t.type({
  page: t.number,
  size: t.number,
});

export type ContractorDispatcherResponse = t.TypeOf<typeof ContractorDispatcherResponse>;

export type ContractorDispatcherBase = t.TypeOf<typeof ContractorDispatcherBase>;

export type ContractorDispatcher = t.TypeOf<typeof ContractorDispatcher>;

export type PaginationParams = t.TypeOf<typeof PaginationParams>;

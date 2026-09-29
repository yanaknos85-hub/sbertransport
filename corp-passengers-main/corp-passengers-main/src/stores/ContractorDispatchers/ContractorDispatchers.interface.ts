import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

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
    id: tt.uuid,
    contractorId: tt.uuid,
    humanReadableId: t.string,
  }),
]);

const Sort = t.partial({
  sorted: t.boolean,
  unsorted: t.boolean,
  empty: t.boolean,
});

const Pageable = t.type({
  sort: Sort,
  offset: t.number,
  pageNumber: t.number,
  pageSize: t.number,
  paged: t.boolean,
  unpaged: t.boolean,
});

export const ContractorDispatcherResponse = t.type({
  empty: t.boolean,
  first: t.boolean,
  last: t.boolean,
  number: t.number,
  numberOfElements: t.number,
  pageable: Pageable,
  size: t.number,
  sort: Sort,
  totalElements: t.number,
  totalPages: t.number,
  content: t.array(ContractorDispatcher),
});

export const PaginationParams = t.type({
  page: t.number,
  size: t.number,
});

export type ContractorDispatcherBase = t.TypeOf<typeof ContractorDispatcherBase>;

export type ContractorDispatcher = t.TypeOf<typeof ContractorDispatcher>;

export type ContractorDispatcherResponse = t.TypeOf<typeof ContractorDispatcherResponse>;

export type PaginationParams = t.TypeOf<typeof PaginationParams>;

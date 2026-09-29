import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

const Sort = t.partial({
  empty: t.boolean,
  sorted: t.boolean,
  unsorted: t.boolean,
});

const Pageable = t.type({
  pageNumber: t.number,
  pageSize: t.number,
  sort: t.array(t.any),
  offset: t.number,
  paged: t.boolean,
  unpaged: t.boolean,
});

const PageSetting = t.type({
  page: t.number,
  size: t.number,
});

const SortSetting = t.type({
  property: t.string,
  directionAsc: t.boolean,
});

export const EtrnSignatureResponse = t.type({
  id: tt.uuid,
  humanReadableId: t.string,
  sla: t.union([t.null, t.string, t.undefined]),
  status: t.string,
  currentTitle: t.union([t.null, t.string, t.undefined]),
  senderName: t.union([t.null, t.string, t.undefined]),
  receiverName: t.union([t.null, t.string, t.undefined]),
  carrierName: t.union([t.null, t.string, t.undefined]),
});

export type EtrnSignatureResponseType = t.TypeOf<typeof EtrnSignatureResponse>;

export const SearchEtrnResponse = t.type({
  totalPages: t.number,
  totalElements: t.number,
  size: t.number,
  content: t.array(EtrnSignatureResponse),
  number: t.number,
  sort: t.union([t.array(t.any), t.undefined]),
  numberOfElements: t.union([t.number, t.undefined]),
  pageable: t.union([Pageable, t.undefined]),
  first: t.union([t.boolean, t.undefined]),
  last: t.union([t.boolean, t.undefined]),
  empty: t.union([t.boolean, t.undefined]),
});

export type SearchEtrnResponseType = t.TypeOf<typeof SearchEtrnResponse>;

export const EtrnFilters = t.partial({
  humanReadableId: t.string,
  statusFilter: t.union([t.string, t.array(t.string), t.null]),
  pageSetting: PageSetting,
  sortSetting: SortSetting,
  organizationId: tt.nullable(t.string),
  lockedByMe: tt.nullable(t.string),
});

export type EtrnFiltersType = t.TypeOf<typeof EtrnFilters>;

export const EtrnTitleDto = t.type({
  fileName: t.string,
  content: t.string,
  creationTime: t.string,
});

export type EtrnTitle = t.TypeOf<typeof EtrnTitleDto>;
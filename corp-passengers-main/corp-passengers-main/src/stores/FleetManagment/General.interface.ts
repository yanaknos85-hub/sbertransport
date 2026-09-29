import * as t from 'io-ts';
import * as tt from '../../utils/io-ts';

const Sorted = t.type({
  sorted: t.boolean,
  unsorted: t.boolean,
  empty: t.boolean,
});

const Pageable = t.type({
  offset: t.number,
  pageNumber: t.number,
  pageSize: t.number,
  paged: t.boolean,
  unpaged: t.boolean,
  sort: Sorted,
});

export const GeneralSearchResponse = t.type({
  totalElements: t.number,
  totalPages: t.number,
  size: t.number,
  number: t.number,
  numberOfElements: t.number,
  first: t.boolean,
  last: t.boolean,
  empty: t.boolean,
  sort: Sorted,
  pageable: t.union([Pageable, t.literal('INSTANCE')]),
});
export type GeneralSearchResponse = t.TypeOf<typeof GeneralSearchResponse>;

export const GeneralResponseContent = t.type({
  id: tt.uuid,
  humanReadableId: t.string,
  officialName: tt.nullable(t.string),
  creationTime: tt.nullable(t.number),
  stateNumber: tt.nullable(t.string),
  personnelNumber: tt.nullable(t.string),
  fullName: tt.nullable(t.string),
});

export const PageSetting = t.strict({
  page: t.number,
  size: t.number,
});
export type PageSetting = t.TypeOf<typeof PageSetting>;

const SortSetting = t.strict({
  property: t.string,
  directionAsc: t.boolean,
});
export type SortSetting = t.TypeOf<typeof SortSetting>;

export const SearchRequest = t.partial({
  fullName: t.string,
  personnelNumber: t.string,
  humanReadableId: t.string,
  sortSetting: SortSetting,
  pageSetting: PageSetting,
});
export type SearchRequest = t.TypeOf<typeof SearchRequest>;

export const AsyncReportResponse = t.type({
  result_url: t.string,
  started: t.boolean,
});

export type AsyncReportResponse = t.TypeOf<typeof AsyncReportResponse>;

export const DateRangeISO = t.strict({
  start: t.string,
  end: t.string,
});
export type DateRangeISO = t.TypeOf<typeof DateRangeISO>;

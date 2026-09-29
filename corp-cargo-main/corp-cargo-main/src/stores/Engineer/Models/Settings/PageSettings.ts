import * as t from 'io-ts';
import { SortSettings } from './SortSettings';

export const PageSettings = t.type({
  page: t.number,
  size: t.number,
});
export type PageSettings = t.TypeOf<typeof PageSettings>;

export const pageAble = t.type({
  sort: SortSettings,
  offset: t.number,
  pageNumber: t.number,
  pageSize: t.number,
  paged: t.boolean,
  unpaged: t.boolean,
});

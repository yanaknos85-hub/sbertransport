import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

import {
  Limit, limitServiceType, limitStatus, limitType, LIMIT_TYPE
} from './Models/Limit';

export const LimitStatus = t.strict({
  id: t.string,
  name: t.string,
  rusName: t.string,
});
export type TLimitStatus = t.TypeOf<typeof LimitStatus>;

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

const PageableUnion = t.union([Pageable, t.string]);

const PageParams = t.type({
  page: t.number,
  size: t.number,
});

export const LimitsSearchQuery = t.partial({
  id: tt.uuid,
  limitId: tt.uuid,
  organization: tt.uuid,
  parentDepartment: tt.uuid,
  limitOwner: tt.optional(tt.uuid),
  humanReadableLimitId: t.string,
  year: t.number,
  limitStatus,
  limitServiceType,
  limitType,
  pagination: tt.optional(PageParams),
});

export type LimitsSearchQuery = t.TypeOf<typeof LimitsSearchQuery>;

export const LimitsSearchResponse = t.type({
  empty: t.boolean,
  first: t.boolean,
  last: t.boolean,
  number: t.number,
  numberOfElements: t.number,
  pageable: PageableUnion,
  size: t.number,
  sort: Sort,
  totalElements: t.number,
  totalPages: t.number,
  content: t.array(Limit),
  id: tt.uuid,
});

export type LimitsSearchResponse = t.TypeOf<typeof LimitsSearchResponse>;

export type DepartmentLimit = Limit & { limitType: LIMIT_TYPE.DEPARTMENT };
